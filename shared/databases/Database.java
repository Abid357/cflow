package databases;

import java.util.List;

public interface Database<T> {
	public int find(Object ...objects);
	public boolean add(Object ...objects);
	public Object get(int index);
	public boolean remove(Object ...objects);
	public boolean loadList();
	public boolean saveList();
	public List<T> getList();
	public boolean isDirty();
	public void setDirty(boolean isDirty);
}
