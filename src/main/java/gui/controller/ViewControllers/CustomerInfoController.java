package gui.controller.ViewControllers;

import be.Customer;
import gui.model.CustomerModel;
import gui.model.UserModel;
import gui.nodes.NotificationBubble;
import gui.util.DialogManager;
import io.github.palexdev.materialfx.controls.*;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import utils.enums.CustomerType;
import utils.enums.ResultState;
import utils.enums.UserRole;
import utils.permissions.AccessChecker;
import utils.permissions.RequiresPermission;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class CustomerInfoController extends ViewController<Customer> implements Initializable {
    @FXML
    private MFXProgressSpinner progressSpinner;
    @FXML
    private Label progressLabel;
    @FXML
    private GridPane gridPane;
    @FXML
    private MFXTableView<Customer> tblCustomers;
    @FXML
    private MFXTextField searchBar;
    @FXML
    private Label expiryLabel;
    private BooleanProperty expiredCustomersProperty = new SimpleBooleanProperty(false);

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private List<Customer> almostExpiredCustomers = new ArrayList<>();
    private final CustomerModel customerModel = CustomerModel.getInstance();
    private boolean hasAccess = false;
    private HashMap<String, Runnable> actions = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                tblCustomers.getItems().setAll(customerModel.searchCustomers(searchBar.getText().toLowerCase().trim())));
        refreshItems(List.copyOf(customerModel.getAll().values()));
        populateTableView();
        progressLabel.visibleProperty().bind(progressSpinner.visibleProperty()); // show label when spinner is visible

        setActions();
    }

    private void populateTableView() {
        customerList.sort(Comparator.comparing(Customer::getLastContract));
        Bindings.bindContentBidirectional(tblCustomers.getItems(), customerList);

        MFXTableColumn<Customer> name = new MFXTableColumn<>("Name", false, Comparator.comparing(Customer::getCustomerName));
        MFXTableColumn<Customer> email = new MFXTableColumn<>("Email", false, Comparator.comparing(Customer::getCustomerEmail));
        MFXTableColumn<Customer> phoneNumber = new MFXTableColumn<>("Phone Number", false, Comparator.comparing(Customer::getCustomerPhoneNumber));
        MFXTableColumn<Customer> address = new MFXTableColumn<>("Address", false, Comparator.comparing(Customer::getCustomerEmail));
        MFXTableColumn<Customer> type = new MFXTableColumn<>("Type", false, Comparator.comparing(Customer::getCustomerType));
        MFXTableColumn<Customer> lastContract = new MFXTableColumn<>("Last Contract", false, Comparator.comparing(Customer::getLastContract));


        name.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(c -> truncateStringWithEllipsis(c.getCustomerName(), 15));
            row.setOnMouseClicked(this::editCustomerAction);
            setRowColour(customer, row);
            return row;
        });

        email.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(Customer::getCustomerEmail);
            row.setOnMouseClicked(this::editCustomerAction);
            setRowColour(customer, row);
            return row;
        });

        phoneNumber.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(Customer::getCustomerPhoneNumber);
            row.setOnMouseClicked(this::editCustomerAction);
            setRowColour(customer, row);
            return row;
        });

        address.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(c -> truncateStringWithEllipsis(c.getCustomerAddress().toString(), 20));
            row.setOnMouseClicked(this::editCustomerAction);
            setRowColour(customer, row);
            return row;
        });

        type.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, CustomerType> row = new MFXTableRowCell<>(Customer::getCustomerType);
            row.setOnMouseClicked(this::editCustomerAction);
            setRowColour(customer, row);
            return row;
        });

        lastContract.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, Date> row = new MFXTableRowCell<>(Customer::getLastContract);
            row.setOnMouseClicked(this::editCustomerAction);
            setRowColour(customer, row);
            return row;
        });


        tblCustomers.getTableColumns().addAll(name, email, phoneNumber, address, type, lastContract);
        tblCustomers.autosizeColumnsOnInitialization();
        tblCustomers.setFooterVisible(false);
    }

    private void setRowColour(Customer customer, MFXTableRowCell<Customer, ?> row) {
        if (customer.getLastContract().before(Date.valueOf(LocalDate.now().minusMonths(47)))) {
            row.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    //region progress methods
    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.progressProperty().unbind();
        progressSpinner.setVisible(visible);
    }

    @Override
    public void bindProgressToTask(Task<ResultState> task) {
        progressSpinner.setProgress(0);
        progressSpinner.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().bind(task.messageProperty());
    }

    @Override
    public void unbindProgress() {
        progressSpinner.progressProperty().unbind();
        progressSpinner.setProgress(100);
        String text = progressLabel.getText();
        progressLabel.textProperty().unbind();
        progressLabel.setText(text);
    }
    //endregion

    @Override
    public void refreshItems(List<Customer> customers) {
        customerList.clear();
        customerList.addAll(customers);

        almostExpiredCustomers.clear();
        for (Customer customer : customerList) {
            if (customer.getLastContract().before(Date.valueOf(LocalDate.now().minusMonths(47)))) {
                almostExpiredCustomers.add(customer);
            }
        }
    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(customerModel.getAll().values()));
        showAlmostExpiredCustomers();
    }

    public void deleteExpiredCustomers() {
        customerModel.deleteExpiredCustomers();
        refreshItems();
    }

    public void showAlmostExpiredCustomers() {
        if (customerModel.getAlmostExpiredCustomers() > 0) {
            expiryLabel.setText("Found " + customerModel.getAlmostExpiredCustomers() + " customer(s) with an almost expired contract");
            expiredCustomersProperty.set(true);
        } else {
            expiryLabel.setText("");
            expiredCustomersProperty.set(false);
        }
    }

    private void editCustomerAction(MouseEvent event) {
        UserRole userRole = UserModel.getLoggedInUser().getUserRole();
        if (event.getClickCount() == 2 && (userRole == UserRole.ADMINISTRATOR || userRole == UserRole.PROJECT_MANAGER)) {
            if (!tblCustomers.getSelectionModel().getSelection().isEmpty()) {
                Customer customer = tblCustomers.getSelectionModel().getSelectedValue();
                DialogManager.getInstance().showChoiceDialog("Extend contract or delete customer",
                        "This customer has " + getTimeUntilContractExpires(customer) + " until their contract expires, what would you like to do?",
                        gridPane, actions);
            }
        }
    }

    private String getTimeUntilContractExpires(Customer customer) {
        String timeUntilContractExpires = "";
        LocalDate contractExpiry = customer.getLastContract().toLocalDate().plusMonths(48);
        LocalDate now = LocalDate.now();
        Period period = Period.between(now, contractExpiry);

        // Years
        if (period.getYears() > 0) {
            timeUntilContractExpires += period.getYears() + " year(s)";
        }

        // Months
        if (period.getMonths() > 0) {
            if (timeUntilContractExpires.length() > 0)
                timeUntilContractExpires += ", ";
            timeUntilContractExpires += period.getMonths() + " month(s)";
        }

        // Days
        if (timeUntilContractExpires.length() > 0)
            timeUntilContractExpires += " and ";
        if (period.getDays() == 0)
            timeUntilContractExpires += "0 days";
        else if (period.getDays() == 1)
            timeUntilContractExpires += "1 day";
        else
            timeUntilContractExpires += period.getDays() + " days";

        return timeUntilContractExpires;
    }

    public void setVisibilityForUserRole() {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        hasAccess = loggedInUserRole == UserRole.ADMINISTRATOR || loggedInUserRole == UserRole.PROJECT_MANAGER;
    }

    // Truncate the string to the specified length and add ellipsis
    private String truncateStringWithEllipsis(String str, int length) {
        if (str.length() > length) {
            return str.substring(0, length) + "...";
        } else {
            return str;
        }
    }

    private void setActions() {
        actions.put("Delete Customer", () -> {
            customerModel.delete(tblCustomers.getSelectionModel().getSelectedValue().getCustomerID());
            almostExpiredCustomers.remove(tblCustomers.getSelectionModel().getSelectedValue());
            refreshItems();
        });

        actions.put("Extend by 48 months", () -> {
            // Extend dateOfLastContract by 48 months
            Customer customer = tblCustomers.getSelectionModel().getSelectedValue();
            customer.setLastContract(Date.valueOf(LocalDate.now()));
            almostExpiredCustomers.remove(tblCustomers.getSelectionModel().getSelectedValue());
            customerModel.update(customer);
            refreshItems();
        });
    }

    public BooleanProperty expiredCustomersProperty() {
        return expiredCustomersProperty;
    }
}

