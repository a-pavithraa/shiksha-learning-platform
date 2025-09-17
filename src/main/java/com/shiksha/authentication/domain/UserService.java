package com.shiksha.authentication.domain;

import com.shiksha.authentication.domain.models.StudentDto;
import com.shiksha.authentication.web.dto.RegisterCommand;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserSubjectRepository userSubjectRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserSubjectRepository userSubjectRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userSubjectRepository = userSubjectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> findActiveUsersByRole(UserRole role) {
        return userRepository.findByRoleAndIsActive(role, true);
    }


    @Transactional
    public User registerUser(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("User with email " + command.email() + " already exists");
        }

        User user = new User(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.firstName(),
                command.lastName(),
                command.role()
        );

        if (command.role() == UserRole.STUDENT && command.gradeLevel() != null) {
            user.setGradeLevel(command.gradeLevel());
        }

        if (command.phone() != null) {
            user.setPhone(command.phone());
        }

        // Save user first to get the ID
        user = userRepository.save(user);

        // Create UserSubject entries if subject IDs are provided
        if (command.subjectIds() != null && !command.subjectIds().isEmpty()) {
            createUserSubjectEntries(user, command.subjectIds(), command.gradeLevel());
        }

        return user;
    }

    private void createUserSubjectEntries(User user, List<Long> subjectIds, Integer gradeLevel) {
        for (Long subjectId : subjectIds) {
            if (user.getRole() == UserRole.STUDENT) {
                // Students: enroll in subjects at their specific grade level
                Integer studentGradeLevel = user.getGradeLevel();
                if (studentGradeLevel == null) {
                    throw new IllegalArgumentException("Student grade level is required for subject enrollment");
                }
                
                if (!userSubjectRepository.existsByUserIdAndSubjectIdAndGradeLevel(
                        user.getId(), subjectId, studentGradeLevel)) {
                    UserSubject userSubject = new UserSubject(user.getId(), subjectId, studentGradeLevel);
                    userSubjectRepository.save(userSubject);
                }
                
            } else if (user.getRole() == UserRole.TEACHER) {
                // Teachers: enroll in subjects for all grade levels (9, 10, 11, 12)
                int[] gradeLevels = {9, 10, 11, 12};
                
                for (int teacherGradeLevel : gradeLevels) {
                    if (!userSubjectRepository.existsByUserIdAndSubjectIdAndGradeLevel(
                            user.getId(), subjectId, teacherGradeLevel)) {
                        UserSubject userSubject = new UserSubject(user.getId(), subjectId, teacherGradeLevel);
                        userSubjectRepository.save(userSubject);
                    }
                }
            }
            // ADMIN role doesn't get subject enrollments by default
        }
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setIsActive(true);
        userRepository.save(user);
    }

    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long userId, String firstName, String lastName, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Cross-module service methods for notification module
    
    /**
     * Finds all students enrolled in a specific subject and grade level
     * Used by notification module to send assignment notifications
     */
    public List<StudentDto> findStudentsBySubjectAndGrade(Long subjectId, Integer gradeLevel) {
        List<User> users = userRepository.findStudentsBySubjectAndGrade(subjectId, gradeLevel);
        return users.stream()
                .map(this::mapUserToStudentDto)
                .toList();
    }

    private StudentDto mapUserToStudentDto(User user) {
        return new StudentDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getGradeLevel(),
                user.getIsActive()
        );
    }

    /**
     * Gets teacher information by ID for cross-module access
     */
    public Optional<User> findTeacherById(Long teacherId) {
        return userRepository.findByIdAndRoleAndIsActive(teacherId, UserRole.TEACHER, true);
    }

    /**
     * Gets student information by ID for cross-module access
     */
    public Optional<User> findStudentById(Long studentId) {
        return userRepository.findByIdAndRoleAndIsActive(studentId, UserRole.STUDENT, true);
    }

    /**
     * Gets user by ID regardless of role
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findByIdAndIsActive(userId, true);
    }

    /**
     * Gets user subject enrollments
     */
    public List<UserSubject> getUserSubjectEnrollments(Long userId) {
        return userSubjectRepository.findByUserId(userId);
    }

    /**
     * Enrolls a user in additional subjects
     */
    @Transactional
    public void enrollUserInSubjects(Long userId, List<Long> subjectIds, Integer gradeLevel) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        if (subjectIds != null && !subjectIds.isEmpty()) {
            createUserSubjectEntries(user, subjectIds, gradeLevel);
        }
    }

}