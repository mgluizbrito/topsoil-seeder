package io.github.mgluizbrito.topsoil_seeder.sample;

import io.github.mgluizbrito.topsoil_seeder.core.engine.SeedEngine;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.h2.tools.Server;

public class Main {
    public static void main(String[] args) {

        // 1. Start JPA (What any app does)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("sample-unit");
        EntityManager em = emf.createEntityManager();


        // 2. USING THE LIB
        try {
            Server server = Server.createWebServer("-webAllowOthers", "-webPort", "8082").start();

            SeedEngine engine = new SeedEngine(em);
            engine.setBasePackage("io.github.mgluizbrito.topsoil_seeder.sample.model");
            engine.seed();

            System.out.println("--- SEEDING FINISHED ---");

            System.out.println("H2 Console available at: " + server.getURL());
            System.out.println("Press ENTER to exit and close bank H2...");
            System.in.read();

            server.stop();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            em.close();
            emf.close();
        }
    }
}
