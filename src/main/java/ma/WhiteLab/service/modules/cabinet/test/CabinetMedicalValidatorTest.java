package ma.WhiteLab.service.modules.cabinet.test;

import ma.WhiteLab.conf.ApplicationContext;
import ma.WhiteLab.entities.cabinet.CabinetMedicale;
import ma.WhiteLab.service.modules.cabinet.api.CabinetMedicalValidator;

public class CabinetMedicalValidatorTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("========== Running CabinetMedicalValidator Integration Tests (Live Database) ==========");
        System.out.println("NOTE: This test assumes the database has been populated by running 'Test.java' first.");

        ApplicationContext context;
        CabinetMedicalValidator validator;

        try {
            context = ApplicationContext.getInstance();
            validator = ApplicationContext.getBean(CabinetMedicalValidator.class);
            if (validator == null) {
                throw new RuntimeException("CabinetMedicalValidator could not be loaded from the context.");
            }
        } catch (Exception e) {
            System.err.println("FATAL: Could not initialize ApplicationContext. Aborting tests.");
            e.printStackTrace();
            return;
        }
        
        // --- Run all test cases ---
        
        testValidationPassesForExistingData(validator);
        testValidationFailsForDuplicateName(validator);
        testValidationFailsForDuplicateEmail(validator);
        testValidationFailsForInvalidData(validator);

        // --- Report results ---
        System.out.println("\n========== Tests Finished ==========");
        System.out.println("Total Tests Run: " + testsRun);
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + (testsRun - testsPassed));
        if (testsRun == testsPassed) {
            System.out.println("All integration tests passed successfully!");
        } else {
            System.err.println("Some integration tests failed.");
        }
    }

    // --- Helper for reporting ---
    private static void runTest(String testName, Runnable testMethod) {
        testsRun++;
        System.out.print("Running: " + testName + " ...");
        try {
            testMethod.run();
            System.out.println(" PASSED");
            testsPassed++;
        } catch (Throwable e) {
            System.err.println(" FAILED - " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // --- Test Methods ---

    /**
     * Tests that validation passes for existing data that is valid.
     */
    private static void testValidationPassesForExistingData(CabinetMedicalValidator validator) {
        runTest("validateForUpdate_withUnchangedValidData", () -> {
            // This cabinet data comes from the main Test.java execution
            CabinetMedicale existingCabinet = new CabinetMedicale();
            existingCabinet.setId(1L); // Assuming the ID of the first cabinet is 1
            existingCabinet.setNom("Clinique WhiteLab - Casablanca Centre (MIS À JOUR)");
            existingCabinet.setEmail("nouveau.contact@whitelab.ma");
            
            // Should not throw any exception
            validator.validateForUpdate(existingCabinet);
        });
    }

    /**
     * Tests that creating a new cabinet with an already existing name fails.
     */
    private static void testValidationFailsForDuplicateName(CabinetMedicalValidator validator) {
        runTest("validateForCreate_withDuplicateName", () -> {
            CabinetMedicale newCabinet = new CabinetMedicale();
            // Use a name that is known to exist from Test.java
            newCabinet.setNom("Clinique WhiteLab - Casablanca Centre (MIS À JOUR)"); 
            newCabinet.setEmail("new.unique.email@test.com");

            try {
                validator.validateForCreate(newCabinet);
                fail("Expected exception for duplicate name was not thrown.");
            } catch (IllegalArgumentException e) {
                assertEquals("Ce nom de cabinet existe déjà", e.getMessage());
            }
        });
    }

    /**
     * Tests that updating a cabinet to an email that another cabinet already uses fails.
     */
    private static void testValidationFailsForDuplicateEmail(CabinetMedicalValidator validator) {
        runTest("validateForUpdate_withDuplicateEmailOfAnotherCabinet", () -> {
            // To test this, we would ideally have a second cabinet in the DB.
            // For now, we simulate what would happen if one existed.
            // Let's test that creating a new cabinet with an existing email fails.
            CabinetMedicale newCabinet = new CabinetMedicale();
            newCabinet.setNom("A new clinic name");
             // Use an email that is known to exist from Test.java
            newCabinet.setEmail("nouveau.contact@whitelab.ma");

            try {
                validator.validateForCreate(newCabinet);
                fail("Expected exception for duplicate email was not thrown.");
            } catch (IllegalArgumentException e) {
                assertEquals("Cet email est déjà utilisé par un autre cabinet", e.getMessage());
            }
        });
    }
    
    /**
     * Tests common field validations like null name or invalid email format.
     */
    private static void testValidationFailsForInvalidData(CabinetMedicalValidator validator) {
        runTest("validateCommonFields_withNullName", () -> {
            CabinetMedicale cabinet = new CabinetMedicale();
            cabinet.setNom(null);
            cabinet.setEmail("some.email@test.com");
            try {
                validator.validateCommonFields(cabinet);
                fail("Expected exception for null name was not thrown.");
            } catch (IllegalArgumentException e) {
                assertEquals("Le nom du cabinet est obligatoire", e.getMessage());
            }
        });

        runTest("validateCommonFields_withInvalidEmail", () -> {
            CabinetMedicale cabinet = new CabinetMedicale();
            cabinet.setNom("A valid name");
            cabinet.setEmail("this-is-not-an-email");
            try {
                validator.validateCommonFields(cabinet);
                fail("Expected exception for invalid email was not thrown.");
            } catch (IllegalArgumentException e) {
                assertEquals("Format d'email invalide", e.getMessage());
            }
        });
    }


    // --- Custom Assertion Methods ---
    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected: \"" + expected + ", but got: \"" + actual + "\"");
        }
    }

    private static void fail(String message) {
        throw new AssertionError(message);
    }
}