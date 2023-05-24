import be.Address;
import be.Customer;
import be.Document;
import be.User;
import bll.manager.CustomerManager;
import bll.manager.DocumentManager;
import bll.pdf.PdfDocumentWrapper;
import bll.pdf.PdfGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import gui.controller.ViewControllers.CustomerInfoController;
import gui.model.CustomerModel;
import gui.model.UserModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.enums.CustomerType;
import utils.enums.ResultState;
import utils.enums.UserRole;
import utils.permissions.AccessChecker;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class WilhelmTest {

    @DisplayName("Tests that the pdf file is saved correctly, and not overwritten")
    @Test
    void pdfFileNameAndPath() {
        // Arrange


        // Act


        // Assert
    }

    @DisplayName("Checks if expired customer contracts are correctly identified")
    @Test
    void checkCustomerExpiry(){
        // Arrange
        UserModel userModel = UserModel.getInstance();
        User user = new User();
        user.setUserRole(UserRole.ADMINISTRATOR);
        userModel.setLoggedInUser(user);

        Address address = new Address("street","number", "city", "zip", "country");
        Customer customer1 = new Customer(UUID.randomUUID(),"name", "email", "phone", address, CustomerType.BUSINESS, Date.valueOf(LocalDate.now()));
        Customer customer2 = new Customer(UUID.randomUUID(),"name", "email", "phone", address, CustomerType.BUSINESS, Date.valueOf(LocalDate.now().minusMonths(46)));
        Customer customer3 = new Customer(UUID.randomUUID(),"name", "email", "phone", address, CustomerType.BUSINESS, Date.valueOf(LocalDate.now().minusMonths(47)));
        Customer customer4 = new Customer(UUID.randomUUID(),"name", "email", "phone", address, CustomerType.BUSINESS, Date.valueOf(LocalDate.now().minusMonths(48)));
        Customer customer5 = new Customer(UUID.randomUUID(),"name", "email", "phone", address, CustomerType.BUSINESS, Date.valueOf(LocalDate.now().minusMonths(49)));

        HashMap<UUID, Customer> allCustomers = new HashMap<>();
        allCustomers.put(customer1.getCustomerID(), customer1);
        allCustomers.put(customer2.getCustomerID(), customer2);
        allCustomers.put(customer3.getCustomerID(), customer3);
        allCustomers.put(customer4.getCustomerID(), customer4);
        allCustomers.put(customer5.getCustomerID(), customer5);

        CustomerManager customerManager = new CustomerManager();

        // Act
        int actual = customerManager.getAlmostExpiredCustomers(allCustomers);

        // Assert
        int expected = 2;
        Assertions.assertEquals(expected, actual);
    }


    @DisplayName("Tests that the user is logged in and out correctly")
    @Test
    void checkLoginAndOut(){
        // Arrange
        UserModel userModel = UserModel.getInstance();
        User user = new User();

        //Act the first
        userModel.setLoggedInUser(user);
        //Assert
        assertSame(UserModel.getLoggedInUser(), user);

        // Act the second
        userModel.logOut();
        // Assert
        assertNull(UserModel.getLoggedInUser());
    }
    @DisplayName("Checks that a salesperson cannot add a document")
    @Test
    void checkAccessLimitation(){
        // Arrange
        UserModel userModel = UserModel.getInstance();
        User user = new User();
        user.setUserRole(UserRole.SALESPERSON);
        userModel.setLoggedInUser(user);
        DocumentManager documentManager = new DocumentManager();

        // Act & Assert
        Assertions.assertEquals(ResultState.NO_PERMISSION, documentManager.add(new Document()));
    }
}
