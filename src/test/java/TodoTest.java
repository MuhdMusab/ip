import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TodoTest {
    @Test
    public void testEmptyDescription() {
        Todo todo = new Todo("", false);
        assertEquals("", todo.description);
    }

    @Test
    public void testChangeIsDone() {
        Todo todo = new Todo("test", false);

        todo.changeIsDone(true);
        assertEquals(true, todo.isDone);

        todo.changeIsDone(false);
        assertEquals(false, todo.isDone);
    }

    @Test
    public void testSuccessfulCanChangeIsDone() {
        Todo todo = new Todo("test", false);

        assertEquals(true, todo.canChangeIsDone(true));

        assertEquals(false, todo.canChangeIsDone(false));

        todo.changeIsDone(true);

        assertEquals(false, todo.canChangeIsDone(true));

        assertEquals(true, todo.canChangeIsDone(false));
    }

    @Test
    public void testToString() {
        Todo todo = new Todo("", false);

        assertEquals("[T][ ] ", todo.toString());

        todo.changeIsDone(true);

        assertEquals("[T][X] ", todo.toString());

        todo.changeIsDone(false);

        assertEquals("[T][ ] ", todo.toString());

        todo.description = "Eat food";

        assertEquals("[T][ ] Eat food", todo.toString());

        todo.changeIsDone(true);

        assertEquals("[T][X] Eat food", todo.toString());
    }
}


