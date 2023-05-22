package gui.controller.ViewControllers;

import be.Address;
import be.Customer;
import gui.model.CustomerModel;
import gui.model.UserModel;
import gui.util.DialogManager;
import gui.util.SceneManager;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import io.github.palexdev.materialfx.controls.MFXTableColumn;
import io.github.palexdev.materialfx.controls.MFXTableView;
import io.github.palexdev.materialfx.controls.MFXTextField;
import io.github.palexdev.materialfx.controls.cell.MFXTableRowCell;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import utils.enums.CustomerType;
import utils.enums.ResultState;
import utils.enums.UserRole;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
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
    private ObservableList<Customer> customerList = FXCollections.observableArrayList();
    private List<Customer> almostExpiredCustomers = new ArrayList<>();
    private final CustomerModel customerModel = CustomerModel.getInstance();
    private boolean hasAccess = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setProgressVisibility(false);

        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                tblCustomers.getItems().setAll(customerModel.searchCustomers(searchBar.getText().toLowerCase().trim())));
        refreshItems();
        populateTableView();
        progressLabel.visibleProperty().bind(progressSpinner.visibleProperty()); // show label when spinner is visible
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

    private void setRowColour(Customer customer, MFXTableRowCell<Customer, ?> row){
        if(customer.getLastContract().before(Date.valueOf(LocalDate.now().minusMonths(47)))){
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
    }

    @Override
    public void refreshItems() {
        refreshItems(List.copyOf(customerModel.getAll().values()));
    }

    public void reloadCustomers() {
        //customerModel.reloadCustomers();
        refreshItems();
    }

    public void deleteExpiredCustomers() {
        customerModel.deleteExpiredCustomers();
        refreshItems();
    }

    public void getAlmostExpiredCustomers() {
        customerModel.getAlmostExpiredCustomers();
    }

    @FXML
    private void editCustomerAction(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (!tblCustomers.getSelectionModel().getSelection().isEmpty()) {
                //TODO: Change to materialfx dialog
                //TODO refresh documents after editing
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Update Customer");
                alert.setHeaderText("Edit or delete a customer");
                alert.setContentText("What would you like to do?");

                // Create the buttons
                ButtonType deleteButton = new ButtonType("Delete Customer");
                ButtonType extendButton = new ButtonType("Extend by 48 months");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                // Set the buttons to the alert
                alert.getButtonTypes().setAll(deleteButton, extendButton, cancelButton);

                // Show the alert and wait for a response
                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent()) {
                    if (result.get() == deleteButton) {
                        customerModel.delete(tblCustomers.getSelectionModel().getSelectedValue().getCustomerID());
                        reloadCustomers();
                        //TODO delete document associated with customer too or replace data with something else?
                    } else if (result.get() == extendButton) {
                        // Extend dateOfLastContract by 48 months
                        tblCustomers.getSelectionModel().getSelectedValue().setLastContract(Date.valueOf(LocalDate.now()));
                        reloadCustomers();
                    } else {
                        alert.close();
                    }
                }
            }
            else {
                DialogManager.getInstance().showError("No customer selected", "Please select a customer", gridPane);
            }
        }
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
}

