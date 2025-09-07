package io.signalzero.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.signalzero.model.DataSourceKey;
import io.signalzero.model.User;
import io.signalzero.service.DataSourceService;
import io.signalzero.service.DataSourceService.DataSourceInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Route(value = "admin/datasources", layout = MainLayout.class)
@PageTitle("Data Source Keys - S1GNAL.ZERO")
public class DataSourceAdminView extends VerticalLayout {

    private final DataSourceService dataSourceService;
    
    private Grid<DataSourceInfo> dataSourceGrid;
    private Grid<DataSourceKey> userKeysGrid;
    private Button addKeyButton;
    private User currentUser;
    
    // Available data sources from application.properties
    private static final String[] AVAILABLE_SERVICES = {
        "reddit", "youtube", "newsapi", "twitter"
    };

    @Autowired
    public DataSourceAdminView(DataSourceService dataSourceService) {
        this.dataSourceService = dataSourceService;
        
        // Get current user from session
        this.currentUser = getCurrentUser();
        
        if (currentUser == null) {
            add(new H2("Access Denied"));
            add(new Paragraph("Please log in to access data source management."));
            return;
        }
        
        setupLayout();
        createHeader();
        createDataSourceStatusGrid();
        createUserKeysSection();
        refreshData();
    }
    
    private User getCurrentUser() {
        // Get user from session
        if (getUI().isPresent()) {
            Object userObj = getUI().get().getSession().getAttribute("user");
            if (userObj instanceof User) {
                return (User) userObj;
            }
        }
        return null;
    }
    
    private void setupLayout() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#f5f5f5");
    }
    
    private void createHeader() {
        // Page header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H1 title = new H1("Data Source Management");
        title.getStyle()
            .set("color", "#2D3748")
            .set("margin", "0")
            .set("font-weight", "600");
        
        Div userInfo = new Div();
        userInfo.add(new Span("Logged in as: " + currentUser.getEmail()));
        userInfo.getStyle()
            .set("color", "#718096")
            .set("font-size", "0.875rem");
        
        header.add(title, userInfo);
        add(header);
        
        // Description
        Paragraph description = new Paragraph(
            "Configure API keys for data sources. User keys take precedence over system defaults. " +
            "Only free tier services are currently available."
        );
        description.getStyle()
            .set("color", "#718096")
            .set("margin", "0 0 2rem 0")
            .set("font-size", "0.9rem");
        add(description);
    }
    
    private void createDataSourceStatusGrid() {
        // Section header
        H3 statusHeader = new H3("Data Source Status");
        statusHeader.getStyle()
            .set("color", "#2D3748")
            .set("margin", "2rem 0 1rem 0")
            .set("font-weight", "600");
        add(statusHeader);
        
        // Status grid
        dataSourceGrid = new Grid<>(DataSourceInfo.class, false);
        dataSourceGrid.setWidthFull();
        dataSourceGrid.setMaxHeight("300px");
        
        dataSourceGrid.addColumn(DataSourceInfo::getServiceName)
            .setHeader("Service")
            .setWidth("150px")
            .setFlexGrow(0);
            
        dataSourceGrid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setWidth("120px")
            .setFlexGrow(0);
            
        dataSourceGrid.addColumn(DataSourceInfo::getStatusText)
            .setHeader("Key Source")
            .setWidth("150px")
            .setFlexGrow(0);
            
        dataSourceGrid.addColumn(info -> info.isAvailable() ? 
            "Configured" : "Not configured")
            .setHeader("Current Key")
            .setFlexGrow(1);
            
        dataSourceGrid.addColumn(DataSourceInfo::getDisplayName)
            .setHeader("Description")
            .setFlexGrow(2);
        
        // Style the grid
        dataSourceGrid.getStyle()
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "8px")
            .set("background", "white");
        
        add(dataSourceGrid);
    }
    
    private void createUserKeysSection() {
        // Section header with add button
        HorizontalLayout sectionHeader = new HorizontalLayout();
        sectionHeader.setWidthFull();
        sectionHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        sectionHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        
        H3 keysHeader = new H3("Your API Keys");
        keysHeader.getStyle()
            .set("color", "#2D3748")
            .set("margin", "2rem 0 0 0")
            .set("font-weight", "600");
        
        addKeyButton = new Button("Add API Key", new Icon(VaadinIcon.PLUS));
        addKeyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addKeyButton.getStyle()
            .set("background", "linear-gradient(45deg, #667eea, #764ba2)")
            .set("border", "none");
        addKeyButton.addClickListener(e -> openAddKeyDialog());
        
        sectionHeader.add(keysHeader, addKeyButton);
        add(sectionHeader);
        
        // User keys grid
        userKeysGrid = new Grid<>(DataSourceKey.class, false);
        userKeysGrid.setWidthFull();
        userKeysGrid.setMaxHeight("400px");
        
        userKeysGrid.addColumn(DataSourceKey::getServiceName)
            .setHeader("Service")
            .setWidth("150px")
            .setFlexGrow(0);
            
        userKeysGrid.addColumn(key -> key.getDisplayKey() != null ? key.getDisplayKey() : maskApiKey(key.getKeyPrefix()))
            .setHeader("API Key")
            .setFlexGrow(1);
            
        userKeysGrid.addColumn(DataSourceKey::getDescription)
            .setHeader("Description")
            .setFlexGrow(2);
            
        userKeysGrid.addColumn(key -> key.getUsageCount())
            .setHeader("Usage")
            .setWidth("100px")
            .setFlexGrow(0);
            
        userKeysGrid.addColumn(key -> key.getCreatedAt() != null ? 
            key.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "")
            .setHeader("Created")
            .setWidth("120px")
            .setFlexGrow(0);
        
        userKeysGrid.addComponentColumn(this::createKeyActions)
            .setHeader("Actions")
            .setWidth("150px")
            .setFlexGrow(0);
        
        // Style the grid
        userKeysGrid.getStyle()
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "8px")
            .set("background", "white")
            .set("margin-top", "1rem");
        
        add(userKeysGrid);
    }
    
    private Span createStatusBadge(DataSourceInfo info) {
        Span badge = new Span(info.isAvailable() ? "Active" : "Inactive");
        badge.getStyle()
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "600")
            .set("text-transform", "uppercase");
            
        if (info.isAvailable()) {
            badge.getStyle()
                .set("background", "#10B981")
                .set("color", "white");
        } else {
            badge.getStyle()
                .set("background", "#F3F4F6")
                .set("color", "#6B7280");
        }
        
        return badge;
    }
    
    private HorizontalLayout createKeyActions(DataSourceKey key) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);
        
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editButton.getStyle().set("color", "#667eea");
        editButton.addClickListener(e -> openEditKeyDialog(key));
        
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        deleteButton.getStyle().set("color", "#e53e3e");
        deleteButton.addClickListener(e -> confirmDeleteKey(key));
        
        actions.add(editButton, deleteButton);
        return actions;
    }
    
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "••••••••";
        }
        
        return apiKey.substring(0, 4) + "••••" + apiKey.substring(apiKey.length() - 4);
    }
    
    private void openAddKeyDialog() {
        Dialog dialog = createKeyDialog(null);
        dialog.open();
    }
    
    private void openEditKeyDialog(DataSourceKey key) {
        Dialog dialog = createKeyDialog(key);
        dialog.open();
    }
    
    private Dialog createKeyDialog(DataSourceKey existingKey) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(existingKey == null ? "Add API Key" : "Edit API Key");
        dialog.setWidth("500px");
        
        // Form layout
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        
        // Form fields
        Select<String> serviceSelect = new Select<>();
        serviceSelect.setLabel("Service");
        serviceSelect.setItems(AVAILABLE_SERVICES);
        serviceSelect.setItemLabelGenerator(service -> {
            switch (service.toLowerCase()) {
                case "reddit": return "Reddit API (Free)";
                case "youtube": return "YouTube Data API (Free Quota)";
                case "newsapi": return "NewsAPI (Free Tier)";
                case "twitter": return "Twitter API (Free Tier)";
                default: return service.toUpperCase();
            }
        });
        
        PasswordField keyField = new PasswordField("API Key");
        keyField.setWidthFull();
        keyField.setRevealButtonVisible(true);
        
        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);
        descriptionField.setPlaceholder("Optional description for this API key...");
        
        // Binder for form validation
        Binder<DataSourceKey> binder = new Binder<>(DataSourceKey.class);
        binder.forField(serviceSelect)
            .withValidator(service -> service != null && !service.isEmpty(), "Service is required")
            .bind(DataSourceKey::getServiceName, DataSourceKey::setServiceName);
            
        binder.forField(keyField)
            .withValidator(key -> key != null && key.length() > 5, "API key must be at least 6 characters")
            .bind(key -> "", (key, value) -> {
                // We'll handle the key value separately since DataSourceKey doesn't expose it
            });
            
        binder.bind(descriptionField, DataSourceKey::getDescription, DataSourceKey::setDescription);
        
        // Pre-populate if editing
        DataSourceKey formBean = existingKey != null ? existingKey : new DataSourceKey();
        if (existingKey != null) {
            serviceSelect.setValue(existingKey.getServiceName());
            keyField.setValue(""); // Don't show existing key for security
            keyField.setPlaceholder("Enter new key to replace existing...");
            descriptionField.setValue(existingKey.getDescription());
            serviceSelect.setReadOnly(true); // Don't allow changing service for existing keys
        }
        
        binder.setBean(formBean);
        
        formLayout.add(serviceSelect, keyField, descriptionField);
        dialog.add(formLayout);
        
        // Buttons
        Button saveButton = new Button(existingKey == null ? "Add Key" : "Update Key");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle()
            .set("background", "linear-gradient(45deg, #667eea, #764ba2)")
            .set("border", "none");
        
        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        saveButton.addClickListener(e -> {
            if (binder.validate().isOk()) {
                try {
                    if (existingKey == null) {
                        // Add new key
                        dataSourceService.saveApiKey(
                            currentUser.getId(),
                            serviceSelect.getValue(),
                            keyField.getValue(),
                            descriptionField.getValue()
                        );
                        showSuccessNotification("API key added successfully");
                    } else {
                        // Update existing key
                        dataSourceService.saveApiKey(
                            currentUser.getId(),
                            existingKey.getServiceName(),
                            keyField.getValue(),
                            descriptionField.getValue()
                        );
                        showSuccessNotification("API key updated successfully");
                    }
                    
                    dialog.close();
                    refreshData();
                } catch (Exception ex) {
                    showErrorNotification("Failed to save API key: " + ex.getMessage());
                }
            }
        });
        
        cancelButton.addClickListener(e -> dialog.close());
        
        dialog.getFooter().add(cancelButton, saveButton);
        
        return dialog;
    }
    
    private void confirmDeleteKey(DataSourceKey key) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Delete API Key");
        
        Div content = new Div();
        content.add(new Paragraph("Are you sure you want to delete the API key for " + 
            key.getServiceName().toUpperCase() + "?"));
        content.add(new Paragraph("This action cannot be undone."));
        
        confirmDialog.add(content);
        
        Button deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        deleteButton.addClickListener(e -> {
            try {
                dataSourceService.revokeApiKey(currentUser.getId(), key.getId());
                showSuccessNotification("API key deleted successfully");
                confirmDialog.close();
                refreshData();
            } catch (Exception ex) {
                showErrorNotification("Failed to delete API key: " + ex.getMessage());
            }
        });
        
        cancelButton.addClickListener(e -> confirmDialog.close());
        
        confirmDialog.getFooter().add(cancelButton, deleteButton);
        confirmDialog.open();
    }
    
    private void refreshData() {
        // Refresh data source status - convert Map to List
        Map<String, DataSourceInfo> dataSourceMap = dataSourceService.getDataSourcesStatus(currentUser.getId());
        List<DataSourceInfo> dataSourceInfo = new ArrayList<>(dataSourceMap.values());
        dataSourceGrid.setDataProvider(new ListDataProvider<>(dataSourceInfo));
        
        // Refresh user keys
        List<DataSourceKey> userKeys = dataSourceService.getUserApiKeys(currentUser.getId());
        userKeysGrid.setDataProvider(new ListDataProvider<>(userKeys));
    }
    
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
