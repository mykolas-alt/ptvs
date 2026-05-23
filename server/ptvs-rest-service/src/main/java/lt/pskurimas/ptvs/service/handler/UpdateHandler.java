package lt.pskurimas.ptvs.service.handler;

import java.util.function.Consumer;

public abstract class UpdateHandler {
    protected <T> void updateIfProvided(Consumer<T> fieldSetter, T updatedField) {
        if (updatedField != null) {
            fieldSetter.accept(updatedField);
        }
    }
}
