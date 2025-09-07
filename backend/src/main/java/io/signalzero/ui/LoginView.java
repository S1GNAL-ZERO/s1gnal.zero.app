package io.signalzero.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import io.signalzero.model.User;
import io.signalzero.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * S1GNAL.ZERO Login View
 * Production-ready login form with proper error handling and validation
 */
@Route("login")
@PageTitle("Login - S1GNAL.ZERO")
@AnonymousAllowed
public class LoginView extends VerticalLayout {
    
    private final UserService userService;
    
    // Form components
    private EmailField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private Div errorMessage;
    
    @Autowired
    public LoginView(UserService userService) {
        this.userService = userService;
        
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)");
        
        createLoginForm();
    }
    
    private void createLoginForm() {
        // Main container
        VerticalLayout loginCard = new VerticalLayout();
        loginCard.setWidth("400px");
        loginCard.setAlignItems(FlexComponent.Alignment.CENTER);
        loginCard.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 8px 32px rgba(0,0,0,0.1)")
                .set("padding", "2rem");
        
        // Logo and title
        H1 title = new H1("S1GNAL.ZERO");
        title.getStyle()
                .set("color", "#667eea")
                .set("font-weight", "bold")
                .set("margin", "0 0 1rem 0")
                .set("text-align", "center");
        
        H2 subtitle = new H2("AI-Powered Manipulation Detection");
        subtitle.getStyle()
                .set("color", "#6b7280")
                .set("font-size", "1.1rem")
                .set("font-weight", "normal")
                .set("margin", "0 0 2rem 0")
                .set("text-align", "center");
        
        // Error message container
        errorMessage = new Div();
        errorMessage.setVisible(false);
        errorMessage.getStyle()
                .set("color", "#dc2626")
                .set("background", "#fef2f2")
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "6px")
                .set("border", "1px solid #fecaca")
                .set("margin-bottom", "1rem")
                .set("font-size", "0.875rem");
        
        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");
        
        // Email field
        emailField = new EmailField("Email");
        emailField.setWidth("100%");
        emailField.setRequired(true);
        emailField.setErrorMessage("Please enter a valid email address");
        emailField.getStyle().set("margin-bottom", "1rem");
        
        // Password field
        passwordField = new PasswordField("Password");
        passwordField.setWidth("100%");
        passwordField.setRequired(true);
        passwordField.setMinLength(6);
        passwordField.setErrorMessage("Password must be at least 6 characters");
        passwordField.getStyle().set("margin-bottom", "1.5rem");
        
        // Login button
        loginButton = new Button("Sign In");
        loginButton.setWidth("100%");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        loginButton.getStyle()
                .set("background", "linear-gradient(135deg, #667eea 0%, #764ba2 100%)")
                .set("margin-bottom", "1rem");
        loginButton.addClickListener(e -> handleLogin());
        
        // Register link
        Div registerSection = new Div();
        registerSection.getStyle().set("text-align", "center");
        
        Span registerText = new Span("Don't have an account? ");
        registerText.getStyle().set("color", "#6b7280");
        
        // TODO: Create RegisterView class
        Anchor registerLink = new Anchor("#", "Sign up here");
        registerLink.getStyle()
                .set("color", "#667eea")
                .set("text-decoration", "none")
                .set("font-weight", "500");
        
        registerSection.add(registerText, registerLink);
        
        // Demo credentials info
        Div demoInfo = new Div();
        demoInfo.getStyle()
                .set("background", "#f3f4f6")
                .set("padding", "1rem")
                .set("border-radius", "6px")
                .set("margin-top", "1.5rem")
                .set("font-size", "0.875rem")
                .set("color", "#6b7280")
                .set("text-align", "center");
        
        H3 demoTitle = new H3("Demo Access");
        demoTitle.getStyle()
                .set("margin", "0 0 0.5rem 0")
                .set("font-size", "0.875rem")
                .set("font-weight", "600")
                .set("color", "#374151");
        
        Div demoCredentials = new Div();
        Span emailSpan = new Span("Email: demo@s1gnalzero.com");
        Span passwordSpan = new Span("Password: demo123");
        passwordSpan.getStyle().set("display", "block");
        demoCredentials.add(emailSpan, passwordSpan);
        
        demoInfo.add(demoTitle, demoCredentials);
        
        // Add components to form
        formLayout.add(emailField, passwordField);
        
        // Add all components to login card
        loginCard.add(
            title,
            subtitle,
            errorMessage,
            formLayout,
            loginButton,
            registerSection,
            demoInfo
        );
        
        // Add keyboard shortcut for Enter key
        passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> handleLogin());
        emailField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> handleLogin());
        
        add(loginCard);
    }
    
    private void handleLogin() {
        // Clear previous error
        hideError();
        
        // Validate fields
        if (!validateFields()) {
            return;
        }
        
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();
        
        // Disable button during processing
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        
        try {
            // Attempt login via UserService
            Optional<User> userOptional = userService.authenticateUser(email, password);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Successful login
                showSuccessNotification("Welcome back, " + user.getFullName() + "!");
                
                // Store user in session (Spring Security integration would be better)
                UI.getCurrent().getSession().setAttribute("user", user);
                UI.getCurrent().getSession().setAttribute("userId", user.getId());
                
                // Redirect to dashboard
                UI.getCurrent().navigate(DashboardView.class);
                
            } else {
                // Invalid credentials
                showError("Invalid email or password. Please try again.");
            }
            
        } catch (Exception e) {
            // Handle any service errors
            showError("Login failed: " + e.getMessage());
        } finally {
            // Re-enable button
            loginButton.setEnabled(true);
            loginButton.setText("Sign In");
        }
    }
    
    private boolean validateFields() {
        boolean isValid = true;
        
        // Validate email
        if (emailField.isEmpty() || !emailField.getValue().contains("@")) {
            emailField.setErrorMessage("Please enter a valid email address");
            emailField.setInvalid(true);
            isValid = false;
        } else {
            emailField.setInvalid(false);
        }
        
        // Validate password
        if (passwordField.isEmpty() || passwordField.getValue().length() < 6) {
            passwordField.setErrorMessage("Password must be at least 6 characters");
            passwordField.setInvalid(true);
            isValid = false;
        } else {
            passwordField.setInvalid(false);
        }
        
        if (!isValid) {
            showError("Please fix the errors above and try again.");
        }
        
        return isValid;
    }
    
    private void showError(String message) {
        errorMessage.setText(message);
        errorMessage.setVisible(true);
    }
    
    private void hideError() {
        errorMessage.setVisible(false);
    }
    
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
