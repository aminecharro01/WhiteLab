package ma.WhiteLab.common.utils;

import ma.WhiteLab.conf.SessionFactory;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Properties;

/**
 * Utilitaire pour créer des instances d'implémentations via réflexion,
 * en se basant sur un fichier de propriétés (ex: beans.properties ou application.properties).
 *
 * Supporte :
 * - Instanciation sans argument (constructeur par défaut)
 * - Instanciation avec arguments (varargs) en choisissant le meilleur constructeur compatible
 * - Compatibilité primitive/wrapper, héritage, interfaces
 * - Vérification stricte du type attendu (cast sécurisé)
 *
 * Utilisé typiquement pour créer des repositories, services, controllers, etc.
 */
public final class FactoryUtils {

    private FactoryUtils() {
        // Classe utilitaire → constructeur privé
    }

    /**
     * Crée une instance d'une implémentation définie dans les Properties.
     *
     * @param props   les propriétés contenant la clé → nom complet de la classe impl
     * @param key     la clé dans les properties (ex: "userRepo", "loginController")
     * @param apiType le type attendu (interface ou classe parente)
     * @param args    arguments à passer au constructeur (peut être vide)
     * @param <T>     type générique attendu
     * @return une instance du type T
     */
    public static <T> T buildImplInstance(Properties props,
                                          String key,
                                          Class<T> apiType,
                                          Object... args) {

        String implClassName = props.getProperty(key);
        if (implClassName == null || implClassName.trim().isBlank()) {
            throw new IllegalArgumentException(
                    "Propriété manquante dans le fichier de configuration : '" + key + "'" +
                            "\nVérifiez que la clé existe et pointe vers une classe valide."
            );
        }
        implClassName = implClassName.trim();

        try {
            System.out.println("[FactoryUtils] Création de l'instance pour la clé '" + key +
                    "' → classe : " + implClassName);

            Class<?> implClass = Class.forName(implClassName);

            Object instance = newInstanceWithArgs(implClass, args);

            // Vérification de type stricte
            if (!apiType.isAssignableFrom(instance.getClass())) {
                throw new ClassCastException(
                        "La classe " + implClassName +
                                " n'implémente pas ou n'étend pas " + apiType.getName()
                );
            }

            System.out.println("[FactoryUtils] Instance créée avec succès : " + instance.getClass().getSimpleName());

            return apiType.cast(instance);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Classe introuvable : " + implClassName +
                            "\nVérifiez le nom complet de la classe dans les properties et qu'elle est dans le classpath.", e);
        } catch (ClassCastException e) {
            throw new RuntimeException(
                    "Incompatibilité de type détectée pour la clé '" + key + "'.", e);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Échec de l'instanciation de " + implClassName +
                            " avec les arguments : " + Arrays.toString(args), e);
        }
    }

    /**
     * Instancie une classe en choisissant le constructeur le plus compatible avec les arguments fournis.
     */
    private static Object newInstanceWithArgs(Class<?> implClass, Object... args) throws Exception {

        if (args == null || args.length == 0) {
            Constructor<?> ctor = implClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        }

        Constructor<?> bestConstructor = null;
        int bestScore = Integer.MAX_VALUE;

        for (Constructor<?> ctor : implClass.getDeclaredConstructors()) {
            Class<?>[] paramTypes = ctor.getParameterTypes();
            if (paramTypes.length != args.length) {
                continue;
            }

            int score = calculateCompatibilityScore(paramTypes, args);
            if (score >= 0 && score < bestScore) {
                bestScore = score;
                bestConstructor = ctor;
            }
        }

        if (bestConstructor == null) {
            throw new NoSuchMethodException(
                    "Aucun constructeur compatible trouvé dans " + implClass.getName() +
                            " pour les arguments : " + Arrays.toString(args)
            );
        }

        bestConstructor.setAccessible(true);
        return bestConstructor.newInstance(args);
    }

    /**
     * Calcule un score de compatibilité entre les types des paramètres et les arguments réels.
     * Plus le score est bas, meilleure est la compatibilité.
     */
    private static int calculateCompatibilityScore(Class<?>[] paramTypes, Object[] args) {
        int score = 0;

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object arg = args[i];

            if (arg == null) {
                if (paramType.isPrimitive()) {
                    return -1; // null interdit pour les types primitifs
                }
                score += 3; // pénalité pour null (moins précis)
                continue;
            }

            Class<?> argType = arg.getClass();

            // Match exact
            if (paramType.equals(argType)) {
                continue;
            }

            // Conversion primitive ↔ wrapper
            if (paramType.isPrimitive()) {
                if (isWrapperOf(argType, paramType)) {
                    score += 1;
                    continue;
                }
                return -1;
            }

            // Héritage ou implémentation d'interface
            if (paramType.isAssignableFrom(argType)) {
                score += calculateInheritanceDistance(argType, paramType);
                continue;
            }

            return -1;
        }

        return score;
    }

    /**
     * Distance dans la hiérarchie d'héritage (plus petite = meilleure compatibilité).
     */
    private static int calculateInheritanceDistance(Class<?> child, Class<?> parent) {
        if (child.equals(parent)) return 0;

        int distance = 0;
        Class<?> current = child;

        while (current != null && !current.equals(parent)) {
            current = current.getSuperclass();
            distance++;
        }

        // Si on n'a pas trouvé dans la chaîne de classes (ex: interface), pénalité fixe
        if (current == null) {
            return parent.isInterface() ? 2 : Integer.MAX_VALUE / 2;
        }

        return distance;
    }

    /**
     * Vérifie si wrapperClass est le wrapper du type primitif donné.
     */
    private static boolean isWrapperOf(Class<?> wrapperClass, Class<?> primitive) {
        return (primitive == int.class     && wrapperClass == Integer.class)   ||
                (primitive == long.class    && wrapperClass == Long.class)      ||
                (primitive == double.class  && wrapperClass == Double.class)    ||
                (primitive == float.class   && wrapperClass == Float.class)     ||
                (primitive == boolean.class && wrapperClass == Boolean.class)   ||
                (primitive == char.class    && wrapperClass == Character.class) ||
                (primitive == byte.class    && wrapperClass == Byte.class)      ||
                (primitive == short.class   && wrapperClass == Short.class);
    }

    // Méthode de test (décommentez pour tester indépendamment)
    /*
    public static void main(String[] args) throws Exception {
        Properties props = ApplicationContext.getInstance().getProperties();

        var userRepo = FactoryUtils.buildImplInstance(
                props,
                "userRepo",
                UserRepo.class,
                SessionFactory.getInstance().getConnection()
        );

        System.out.println("Repository créé : " + userRepo);
        System.out.println("Utilisateur ID 1 : " + userRepo.findById(1L));
    }
    */
}