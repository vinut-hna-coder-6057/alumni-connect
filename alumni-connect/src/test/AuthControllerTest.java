public class AuthControllerTest {
    @Test
void loginShouldReturnInvalidCredentialsWhenUserDoesNotExist() {

    // Arrange

    UserRepository repository =
            Mockito.mock(UserRepository.class);

    BCryptPasswordEncoder encoder =
            Mockito.mock(BCryptPasswordEncoder.class);

    AuthController controller =
            new AuthController(
                    repository,
                    encoder
            );

    User loginUser =
            new User();

    loginUser.setEmail("unknown@gmail.com");
    loginUser.setRole("STUDENT");
    loginUser.setPassword("password123");

    when(
            repository.findByEmailAndRole(
                    "unknown@gmail.com",
                    "STUDENT"
            )
    ).thenReturn(Optional.empty());

    // Act

    Object result =
            controller.login(loginUser);

    // Assert

    assertEquals(
            "Invalid credentials",
            result
    );
}
    @Test
    void loginShouldReturnInvalidCredentialsWhenUserDoesNotExist() {

        // Arrange

        UserRepository repository =
                Mockito.mock(UserRepository.class);

        BCryptPasswordEncoder encoder =
                Mockito.mock(BCryptPasswordEncoder.class);

        AuthController controller =
                new AuthController(
                        repository,
                        encoder
                );

        User loginUser =
                new User();

        loginUser.setEmail("unknown@gmail.com");
        loginUser.setRole("STUDENT");
        loginUser.setPassword("password123");

        when(
                repository.findByEmailAndRole(
                        "unknown@gmail.com",
                        "STUDENT"
                )
        ).thenReturn(Optional.empty());

        // Act

        Object result =
                controller.login(loginUser);

        // Assert

        assertEquals(
                "Invalid credentials",
                result
        );
    }
    @Test
void signupShouldEncodePasswordAndSetPendingStatus() {

    UserRepository repository =
            Mockito.mock(UserRepository.class);

    BCryptPasswordEncoder encoder =
            Mockito.mock(BCryptPasswordEncoder.class);

    AuthController controller =
            new AuthController(
                    repository,
                    encoder
            );

    User user = new User();

    user.setEmail("newstudent@gmail.com");
    user.setRole("STUDENT");
    user.setPassword("password123");

    when(
            repository.findByEmail(
                    "newstudent@gmail.com"
            )
    ).thenReturn(Optional.empty());

    when(
            encoder.encode("password123")
    ).thenReturn("encodedPassword");

    Object result =
            controller.signup(user);

    assertEquals(
            "Signup successful",
            result
    );

    assertEquals(
            "PENDING",
            user.getStatus()
    );

    assertEquals(
            "encodedPassword",
            user.getPassword()
    );

    Mockito.verify(repository).save(user);
}
@Test
void loginShouldReturnJwtWhenUserIsApproved() {

    UserRepository repository =
            Mockito.mock(UserRepository.class);

    BCryptPasswordEncoder encoder =
            Mockito.mock(BCryptPasswordEncoder.class);

    AuthController controller =
            new AuthController(
                    repository,
                    encoder
            );

    User existingUser =
            new User();

    existingUser.setEmail("student@gmail.com");
    existingUser.setRole("STUDENT");
    existingUser.setStatus("APPROVED");
    existingUser.setPassword("encodedPassword");

    User loginUser =
            new User();

    loginUser.setEmail("student@gmail.com");
    loginUser.setRole("STUDENT");
    loginUser.setPassword("password123");

    when(
            repository.findByEmailAndRole(
                    "student@gmail.com",
                    "STUDENT"
            )
    ).thenReturn(
            Optional.of(existingUser)
    );

    Object result =
            controller.login(loginUser);

    assertEquals(
            "WAIT_APPROVAL",
            result
    );
}
}
