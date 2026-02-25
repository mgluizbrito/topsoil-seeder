package io.github.mgluizbrito.topsoil_seeder_sample;

import io.github.mgluizbrito.topsoil_seeder.engine.SeedEngine;
import io.github.mgluizbrito.topsoil_seeder_sample.model.Employee;
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
            engine.setBasePackage("io.github.mgluizbrito.topsoil_seeder_sample.model");
            engine.seed();

            System.out.println("--- SEEDING FINISHED ---");

            // 3. Check the database to see if the data is there
            em.createQuery("SELECT e FROM Employee e", Employee.class)
                    .getResultList()
                    .forEach(emp -> {
                        System.out.printf("FUNC: %s | Depto: %s%n",
                                emp.getName(), emp.getDepartment().getName());
                    });

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
