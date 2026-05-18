package timetableoptimizer;

public interface Manager {
    void listAll();
    void edit(String ID, String field, String value);
    void delete(String ID);
}
