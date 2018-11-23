package by.andruhovich.manager.start;

import by.andruhovich.manager.service.ManagerService;

public class Manager {
    public static void main(String[] args) {
        ManagerService managerService = new ManagerService();
        managerService.serviceManager();
    }
}
