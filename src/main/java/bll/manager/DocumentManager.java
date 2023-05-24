package bll.manager;

import be.Document;
import be.User;
import utils.enums.ResultState;
import utils.enums.UserRole;
import bll.IManager;
import dal.factories.DAOFactory;
import dal.dao.DocumentDAO;
import gui.model.UserModel;
import utils.permissions.AccessChecker;
import utils.permissions.RequiresPermission;

import java.util.Map;
import java.util.UUID;

public class DocumentManager implements IManager<Document> {
    private final DocumentDAO dao;
    private final AccessChecker checker = new AccessChecker();

    public DocumentManager() {
        dao = (DocumentDAO) DAOFactory.createDAO(DAOFactory.DAOType.DOCUMENT);
    }

    /**
     * Add a document to the database
     * @param document document to add
     * @return ResultState / NO_PERMISSION
     */
    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER, UserRole.TECHNICIAN})
    public ResultState add(Document document) {
        if (checker.hasAccess(this.getClass())) {
            return dao.add(document);
        }
        else {
            return ResultState.NO_PERMISSION;
        }
    }

    /**
     * Update a document in the database
     * @param document document to update
     * @return ResultState / NO_PERMISSION
     */
    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER})
    public ResultState update(Document document) {
        if (document.getTechnicians().contains(UserModel.getLoggedInUser()) || checker.hasAccess(this.getClass())) {
            return dao.update(document);
        }
        else {
            return ResultState.NO_PERMISSION;
        }
    }

    /**
     * Delete a document from the database
     * @param id id of the document to delete
     * @return ResultState / NO_PERMISSION
     */
    @Override
    @RequiresPermission({UserRole.ADMINISTRATOR, UserRole.PROJECT_MANAGER})
    public ResultState delete(UUID id) {
        if (checker.hasAccess(this.getClass())) {
            return dao.delete(id);
        }
        else {
            return ResultState.NO_PERMISSION;
        }
    }

    /**
     * Get all documents from the database
     * @return Map<UUID, Document> / NO_PERMISSION
     */
    @Override
    public Map<UUID, Document> getAll() {
        return dao.getAll();
    }

    /**
     * Get a document by id from the database
     * @param id id of the document to get
     * @return Document / NO_PERMISSION
     */
    @Override
    public Document getById(UUID id) {
        return dao.getById(id);
    }

    /**
     * Assign a user to a document
     * @param user user to assign
     * @param document document to assign to
     * @param isAssigning true if assigning, false if unassigning
     */
    public void assignUserToDocument(User user, Document document, boolean isAssigning) {
        dao.assignUserToDocument(user, document, isAssigning);
    }

    /**
     * Add a drawing to a document
     * @param document document to add to
     * @param drawing drawing to add
     */
    public void addDrawingToDocument(Document document, String drawing) {
        dao.addDrawingToDocument(document, drawing);
    }

    /**
     * Get the drawing from a document
     * @param document document to get from
     * @return String
     */
    public String getDrawingFromDocument(Document document) {
        return dao.getDrawingOnDocument(document);
    }
}
