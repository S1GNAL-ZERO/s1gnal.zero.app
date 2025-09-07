package io.signalzero.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.signalzero.model.SubscriptionTier;
import io.signalzero.model.User;
import io.signalzero.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Route("register")
@PageTitle("Register - S1GNAL.ZERO")
@AnonymousAllowed
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;

    private EmailField emailField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private TextField fullNameField;
    private Select<SubscriptionTier> subscriptionTierSelect;
    private Button registerButton;
    private Button loginButton;

    @Autowired
    public RegisterView(UserService userService) {
        this.userService = userService;
        setupLayout();
        createRegistrationForm();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Check if user is already logged in
        if (getUI().isPresent() && getUI().get().getSession().getAttribute("user") != null) {
            event.forwardTo(DashboardView.class);
        }
    }

    private void setupLayout() {
        setSizeFull();
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setAlignItems(FlexComponent.Alignment.CENTER);
        
        // Add gradient background
        getStyle().set("background", 
            "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        getStyle().set("min-height", "100vh");
    }

    private void createRegistrationForm() {
        // Main container
        Div container = new Div();
        container.getStyle()
            .set("background", "rgba(255, 255, 255, 0.95)")
            .set("border-radius", "12px")
            .set("box-shadow", "0 8px 32px rgba(0, 0, 0, 0.1)")
            .set("backdrop-filter", "blur(10px)")
            .set("padding", "2rem")
            .set("width", "400px")
            .set("max-width", "90vw");

        // Header
        H2 title = new H2("Create Account");
        title.getStyle()
            .set("color", "#2D3748")
            .set("text-align", "center")
            .set("margin", "0 0 1.5rem 0")
            .set("font-weight", "600");

        Span subtitle = new Span("Join S1GNAL.ZERO to detect manipulation");
        subtitle.getStyle()
            .set("color", "#718096")
            .set("text-align", "center")
            .set("display", "block")
            .set("margin-bottom", "2rem")
            .set("font-size", "0.875rem");

        // Form Layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        // Form Fields
        createFormFields();
        formLayout.add(fullNameField, emailField, passwordField, confirmPasswordField, subscriptionTierSelect);

        // Buttons
        createButtons();
        
        Div buttonContainer = new Div();
        buttonContainer.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "1rem")
            .set("margin-top", "1.5rem");
        buttonContainer.add(registerButton, createLoginLink());

        // Assemble container
        container.add(title, subtitle, formLayout, buttonContainer);
        add(container);
    }

    private void createFormFields() {
        // Full Name Field
        fullNameField = new TextField("Full Name");
        fullNameField.setWidthFull();
        fullNameField.setRequired(true);
        fullNameField.setErrorMessage("Full name is required");
        styleField(fullNameField);

        // Email Field
        emailField = new EmailField("Email Address");
        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setErrorMessage("Valid email is required");
        styleField(emailField);

        // Password Field
        passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setMinLength(6);
        passwordField.setErrorMessage("Password must be at least 6 characters");
        passwordField.setHelperText("Minimum 6 characters");
        styleField(passwordField);

        // Confirm Password Field
        confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setErrorMessage("Passwords do not match");
        styleField(confirmPasswordField);

        // Subscription Tier Select
        subscriptionTierSelect = new Select<>();
        subscriptionTierSelect.setLabel("Subscription Plan");
        subscriptionTierSelect.setItems(SubscriptionTier.FREE, SubscriptionTier.PRO, SubscriptionTier.BUSINESS);
        subscriptionTierSelect.setValue(SubscriptionTier.FREE);
        subscriptionTierSelect.setItemLabelGenerator(tier -> {
            switch (tier) {
                case FREE: return "FREE - 3 analyses/month";
                case PRO: return "PRO - 100 analyses/month ($9.99)";
                case BUSINESS: return "BUSINESS - 1,000 analyses/month ($49.99)";
                default: return tier.name();
            }
        });
        styleField(subscriptionTierSelect);
    }

    private void styleField(com.vaadin.flow.component.Component field) {
        field.getElement().getStyle()
            .set("--vaadin-field-default-width", "100%");
    }

    private void createButtons() {
        // Register Button
        registerButton = new Button("Create Account");
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.getStyle()
            .set("background", "linear-gradient(45deg, #667eea, #764ba2)")
            .set("border", "none")
            .set("color", "white")
            .set("font-weight", "600")
            .set("padding", "0.75rem")
            .set("border-radius", "8px")
            .set("cursor", "pointer");

        registerButton.addClickListener(e -> handleRegistration());
    }

    private Div createLoginLink() {
        Div loginLinkContainer = new Div();
        loginLinkContainer.getStyle()
            .set("text-align", "center")
            .set("margin-top", "1rem");

        Span loginText = new Span("Already have an account? ");
        loginText.getStyle().set("color", "#718096");

        loginButton = new Button("Sign In");
        loginButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginButton.getStyle()
            .set("color", "#667eea")
            .set("text-decoration", "none")
            .set("font-weight", "600")
            .set("padding", "0")
            .set("margin", "0")
            .set("background", "none")
            .set("border", "none")
            .set("cursor", "pointer");

        loginButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));

        loginLinkContainer.add(loginText, loginButton);
        return loginLinkContainer;
    }

    private void handleRegistration() {
        if (!validateForm()) {
            return;
        }

        registerButton.setText("Creating Account...");
        registerButton.setEnabled(false);

        try {
            // Register user with UserService
            User registeredUser = userService.registerUser(
                emailField.getValue().trim().toLowerCase(),
                passwordField.getValue(),
                fullNameField.getValue().trim()
            );

            // Update subscription tier if not FREE
            if (subscriptionTierSelect.getValue() != SubscriptionTier.FREE) {
                registeredUser = userService.updateSubscriptionTier(
                    registeredUser.getId(), 
                    subscriptionTierSelect.getValue()
                );
            }

            if (registeredUser != null) {
                // Show success message
                Notification success = Notification.show(
                    "Account created successfully! Please log in.", 
                    3000, 
                    Notification.Position.TOP_CENTER
                );
                success.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Redirect to login after short delay
            getUI().ifPresent(ui -> {
                ui.access(() -> {
                    try {
                        Thread.sleep(1500);
                        ui.navigate(LoginView.class);
                    } catch (InterruptedException ex) {
                        ui.navigate(LoginView.class);
                    }
                });
            });
        }

        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
            resetRegisterButton();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Validate full name
        if (fullNameField.getValue() == null || fullNameField.getValue().trim().isEmpty()) {
            fullNameField.setInvalid(true);
            errors.append("Full name is required. ");
            isValid = false;
        } else {
            fullNameField.setInvalid(false);
        }

        // Validate email
        if (emailField.getValue() == null || emailField.getValue().trim().isEmpty()) {
            emailField.setInvalid(true);
            errors.append("Email is required. ");
            isValid = false;
        } else if (!isValidEmail(emailField.getValue())) {
            emailField.setInvalid(true);
            errors.append("Valid email is required. ");
            isValid = false;
        } else {
            emailField.setInvalid(false);
        }

        // Validate password
        if (passwordField.getValue() == null || passwordField.getValue().length() < 6) {
            passwordField.setInvalid(true);
            errors.append("Password must be at least 6 characters. ");
            isValid = false;
        } else {
            passwordField.setInvalid(false);
        }

        // Validate password confirmation
        if (confirmPasswordField.getValue() == null || 
            !confirmPasswordField.getValue().equals(passwordField.getValue())) {
            confirmPasswordField.setInvalid(true);
            errors.append("Passwords do not match. ");
            isValid = false;
        } else {
            confirmPasswordField.setInvalid(false);
        }

        // Show validation errors
        if (!isValid) {
            showError(errors.toString().trim());
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 &&
               !email.startsWith("@") &&
               !email.endsWith("@");
    }

    private void showError(String message) {
        Dialog errorDialog = new Dialog();
        errorDialog.setHeaderTitle("Registration Error");
        
        Span errorMessage = new Span(message);
        errorMessage.getStyle()
            .set("color", "#e53e3e")
            .set("margin", "1rem");
        
        Button closeButton = new Button("OK");
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        closeButton.addClickListener(e -> errorDialog.close());
        
        errorDialog.add(errorMessage);
        errorDialog.getFooter().add(closeButton);
        errorDialog.open();
    }

    private void resetRegisterButton() {
        registerButton.setText("Create Account");
        registerButton.setEnabled(true);
    }
}
