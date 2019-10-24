package app;

import app.entities.*;
import org.hibernate.StatelessSession;
import org.w3c.dom.ls.LSOutput;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Engine implements Runnable{

    public Engine(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private final EntityManager entityManager;

    @Override
    public void run() {
        getEmployeeWithProject();
    }


    // 2. Remove Objects - OK
    private void removeObjects(){
        this.entityManager.getTransaction().begin();
        List<Town> towns = this.entityManager.createQuery("FROM Town", Town.class)
                .getResultList();

        for(Town town : towns){
            if(town.getName().length() > 5){
                this.entityManager.detach(town);
            } else {
                town.setName(town.getName().toLowerCase());
            }

        }

        // check if entities are detached
        for(Town town : towns){
            if(this.entityManager.contains(town)){
                System.out.println(town.getName());
            }
        }

        // update database
        this.entityManager.getTransaction().commit();


    }

    // 3. Contains Employee - OK
    private void containsEmployee(){
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();

        try{
            this.entityManager.getTransaction().begin();
            Employee employee = this.entityManager
                    .createQuery("FROM Employee WHERE concat(first_name, ' ', last_name) = :name", Employee.class)
                    .setParameter("name", name)
                    .getSingleResult();
            System.out.println("Yes");
        } catch (NoResultException nre){
            System.out.println("No");
        }

        this.entityManager.getTransaction().commit();
    }

    // 4. Employee with Salary over 50000 - ОК
    private void employeeSalaryOver(){
        this.entityManager.getTransaction().begin();
        List<Employee> employees = this.entityManager.createQuery("FROM Employee WHERE salary>50000", Employee.class).getResultList();

        for(Employee employee : employees){
            System.out.println(employee.getFirstName());
        }
        this.entityManager.getTransaction().commit();
    }

    // 5. Employees from Department - OK
    private void employeesFromDepartment(){
        this.entityManager.getTransaction().begin();

        Department department = this.entityManager.createQuery("FROM Department WHERE name='Research and Development'", Department.class).getSingleResult();

        Set<Employee> test = department.getEmployees();

        test.stream().sorted(Comparator.comparing(Employee::getSalary)).forEach(employee -> System.out.println(employee.getFirstName() + " " + employee.getLastName() + " from " +
                department.getName() + " $" + employee.getSalary()));


        this.entityManager.getTransaction().commit();

    }

    // 6. Adding new address - OK
    private void addingNewAddress(){
        Scanner scanner = new Scanner(System.in);
        String lastNameInput = scanner.nextLine();

        this.entityManager.getTransaction().begin();

        Town town = this.entityManager.createQuery("FROM Town WHERE name='Sofia'", Town.class).getSingleResult();
        Address address = new Address();
        address.setText("Vitoshka 15");
        address.setTown(town);

        Employee employee = this.entityManager.createQuery("FROM Employee WHERE lastName=:name" , Employee.class)
                .setParameter("name", lastNameInput)
                .getSingleResult();

        this.entityManager.detach(employee.getAddress());
        employee.setAddress(address);
        this.entityManager.persist(address);
        this.entityManager.merge(employee);

        this.entityManager.getTransaction().commit();
    }

    // 7. Find addresses - OK
    private void findAddresses(){
        this.entityManager.getTransaction().begin();
        List<Address> allAddresses = this.entityManager.createQuery("FROM Address", Address.class).getResultList();

        orderByNumberEmployees(allAddresses);

        int count=0;
        for(Address addr: allAddresses){
            if(count>10){
                break;
            }
            Set<Employee> address = addr.getEmployees();
            System.out.println(addr.getText() + " " + addr.getTown().getName() +" - "+ address.size() + " employees");

            count++;
        }
        this.entityManager.getTransaction().commit();
    }

    private static void orderByNumberEmployees(List<Address> addresses) {

        Collections.sort(addresses, new Comparator<Address>() {

            @Override
            public int compare(Address t, Address t1) {
                return t1.getEmployees().size() - t.getEmployees().size();
            }
        });

    }

    private static void orderByTownId(List<Address> addresses) {

        Collections.sort(addresses, new Comparator<Address>() {

            @Override
            public int compare(Address t, Address t1) {
                return t.getEmployees().size() - t1.getEmployees().size();
            }
        });

    }

    // 8. Get Employee with Project
    private void getEmployeeWithProject(){
        this.entityManager.getTransaction().begin();
        System.out.println("Please enter employee id: \n");
        Scanner scanner = new Scanner(System.in);
        int id = scanner.nextInt();

        List<Employee> employees = this.entityManager.createQuery("FROM Employee WHERE id=:id ORDER BY firstName", Employee.class)
                .setParameter("id", id)
                .getResultList();

        for(Employee employee : employees){
            System.out.println(employee.getFirstName() + " " + employee.getLastName() + " " + employee.getJobTitle());

            Set<Project> employeeProjects = employee.getProjects();
            // sorting
            List<Project> projectList = employeeProjects.stream().sorted((e1, e2) ->
                    e1.getName().compareTo(e2.getName())).collect(Collectors.toList());

            for(Project project : projectList){
                System.out.println("    " + project.getName());
            }

        }
    }
}
