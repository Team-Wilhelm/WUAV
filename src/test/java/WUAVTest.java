import be.Address;
import be.Customer;
import bll.manager.CustomerManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.HashPasswordHelper;
import utils.enums.CustomerType;
import utils.enums.UserRole;
import utils.permissions.AccessChecker;
import utils.permissions.RequiresPermission;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class WUAVTest {

    @DisplayName("Checks if expired customer contracts are correctly identified")
    @Test
    void checkCustomerExpiry(){
        // Arrange
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
        int actual = customerManager.calculateExpiredCustomers(allCustomers);

        // Assert
        int expected = 2;
        Assertions.assertEquals(expected, actual);
    }

    //TODO please check that I did this correctly
    @DisplayName("Checks if password hashing works correctly")
    @Test
    void checkPasswordHashing(){
        // Arrange
        HashPasswordHelper hashPasswordHelper = new HashPasswordHelper();
        String password = "password";
        byte[][] hashedPassword = hashPasswordHelper.hashPassword(password);

        // Act
        boolean actual = (Arrays.equals(hashedPassword[0], hashPasswordHelper.hashPassword(password, hashedPassword[1])));

        // Assert
        Assertions.assertTrue(actual);
    }


    @DisplayName("Checks the UserRole validation in the AccessChecker class")
    @Test
    void checkAccessLimitationTrue() {
        //Arrange
        AccessChecker accessChecker = new AccessChecker();

        //Act & Assert
        Assertions.assertTrue(accessChecker.calculateAccess(TestClass.class, UserRole.ADMINISTRATOR));
    }

    @DisplayName("Checks the UserRole validation in the AccessChecker class")
    @Test
    void checkAccessLimitationFalse() {
        //Arrange
        AccessChecker accessChecker = new AccessChecker();

        //Act & Assert
        Assertions.assertFalse(accessChecker.calculateAccess(TestClass.class, UserRole.SALESPERSON));
    }

    /**
     * This is a test class used to perform the test of the calculateAccess method in the AccessChecker class.
     * It contains a method with the RequiresPermission annotation.
     */
    protected class TestClass {
        @RequiresPermission(UserRole.ADMINISTRATOR)
        public String accessCheckTestMethod(){
            return "Hello World!";
        }
    }

}
