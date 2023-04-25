package gui.model;

public class Model {
    private static Model instance;
    private CustomerModel customerModel;
    private UserModel userModel;
    private DocumentModel documentModel;

    private Model() {
        customerModel = new CustomerModel();
        userModel = new UserModel();
        documentModel = new DocumentModel();
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public CustomerModel getCustomerModel() {
        return customerModel;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public DocumentModel getDocumentModel() {
        return documentModel;
    }
}
