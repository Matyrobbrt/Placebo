package shadows.placebo.json;

import com.google.gson.JsonObject;
import org.slf4j.Logger;

/**
 * An abstraction interface used to check whether an JSON resource should be loaded.
 */
public interface ConditionHelper {
    /**
     * A {@link ConditionHelper} that always allows the resource to be loaded.
     */
    ConditionHelper NOOP = (object, conditionsArrayName, logger) -> true;

    /**
     * Checks whether the conditions in the given {@code object} allow it to be loaded.
     *
     * @param object              the object whose conditions to check
     * @param conditionsArrayName the name of the conditions array
     * @param logger              a logger used for error reporting
     * @return whether the {@code object} should be loaded
     */
    boolean shouldBeLoaded(JsonObject object, String conditionsArrayName, Logger logger);
}
