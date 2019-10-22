package app;

import javax.persistence.EntityManager;

public class Engine implements Runnable{

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private final EntityManager entityManager;

    @Override
    public void run() {

    }
}
