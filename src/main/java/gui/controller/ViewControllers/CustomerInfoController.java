package gui.controller.ViewControllers;

import be.Address;
import be.Customer;
import gui.model.CustomerModel;
import gui.model.UserModel;
import gui.util.DialogueManager;
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
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import utils.enums.CustomerType;
import utils.enums.ResultState;
import utils.enums.UserRole;

import java.net.URL;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

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
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(Customer::getCustomerName);
            row.setOnMouseClicked(this::editCustomerAction);
            return row;
        });

        email.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(Customer::getCustomerEmail);
            row.setOnMouseClicked(this::editCustomerAction);
            return row;
        });

        phoneNumber.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, String> row = new MFXTableRowCell<>(Customer::getCustomerPhoneNumber);
            row.setOnMouseClicked(this::editCustomerAction);
            return row;
        });

        address.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, Address> row = new MFXTableRowCell<>(Customer::getCustomerAddress);
            row.setOnMouseClicked(this::editCustomerAction);
            return row;
        });

        type.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, CustomerType> row = new MFXTableRowCell<>(Customer::getCustomerType);
            row.setOnMouseClicked(this::editCustomerAction);
            return row;
        });

        lastContract.setRowCellFactory(customer -> {
            MFXTableRowCell<Customer, Date> row = new MFXTableRowCell<>(Customer::getLastContract);
            row.setOnMouseClicked(this::editCustomerAction);
            return row;
        });


        tblCustomers.getTableColumns().addAll(name, email, phoneNumber, address, type, lastContract);
        tblCustomers.autosizeColumnsOnInitialization();
        tblCustomers.setFooterVisible(false);
    }

    //region progress methods
    @Override
    public void setProgressVisibility(boolean visible) {
        progressSpinner.progressProperty().unbind();
        progressSpinner.setVisible(visible);
        progressLabel.setVisible(visible);
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

    @FXML
    private void editCustomerAction(MouseEvent event) {
        if (event.getClickCount() == 2) {
            if (!tblCustomers.getSelectionModel().getSelection().isEmpty()) {
                //TODO: Open edit customer window/popup
            }
            else {
                DialogueManager.getInstance().showError("No customer selected", "Please select a customer", gridPane);
            }
        }
    }

    public void setVisibilityForUserRole() {
        UserRole loggedInUserRole = UserModel.getLoggedInUser().getUserRole();
        hasAccess = loggedInUserRole == UserRole.ADMINISTRATOR || loggedInUserRole == UserRole.PROJECT_MANAGER;
    }
}

