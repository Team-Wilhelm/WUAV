package bll.manager;

import be.Document;
import be.User;
import be.enums.UserRole;
import bll.IManager;
import dal.DAOFactory;
import dal.dao.DocumentDAO;
import gui.model.UserModel;
import utils.permissions.AccessChecker;
import utils.permissions.RequiresPermission;

import java.util.Map;
import java.util.UUID;

public class DocumentManager implements IManager<Document> {
    private DocumentDAO dao;
    private AccessChecker checker = new AccessChecker();

    public DocumentManager() {
        dao = (DocumentDAO) DAOFactory.createDAO(DAOFactory.DAOType.DOCUMENT);
    }

    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER, UserRole.TECHNICIAN})
    public String add(Document document) {
        if (checker.hasAccess(this.getClass())) {
            return dao.add(document);
        }
        else {
            return "No Permission";}
    }

    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER})
    public String update(Document document) {
        if (document.getTechnicians().contains(UserModel.getLoggedInUser()) || checker.hasAccess(this.getClass())) {
            return dao.update(document);
        }
        else {
            return "No Permission";}
    }

    @Override
    public String delete(UUID id) {
        if (getById(id).getTechnicians().contains(UserModel.getLoggedInUser()) || checker.hasAccess(this.getClass())) {
            return dao.delete(id);
        }
        else {
            return "No Permission";}
    }

    @Override
    public Map<UUID, Document> getAll() {
        return dao.getAll();
    }

    @Override
    public Document getById(UUID id) {
        return dao.getById(id);
    }

    public void assignUserToDocument(User user, Document document, boolean isAssigning){
        dao.assignUserToDocument(user, document, isAssigning);
    }
}