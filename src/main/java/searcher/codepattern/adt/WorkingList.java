package searcher.codepattern.adt;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * 一个用于工作表算法的数据结构
 * @param <E> 工作表元素的数据类型
 * @author huacy
 */
public class WorkingList<E> {
	private Queue<E> queue = new LinkedList<>();
	private Set<E> belong = new HashSet<>();

	/**
	 * 向工作表中添加一个元素，如果该元素已存在于工作表中，则忽略本次操作
	 * @param item 向工作表中添加的元素
	 * @return 添加成功返回<code>true</code>，如果表中已有该元素返回<code>false</code>
	 */
	public boolean push(E item) {
		if (belong.contains(item)) return false;
		queue.add(item);
		belong.add(item);
		return true;
	}

	/**
	 * 从工作表中取出一个元素，取出的元素会从工作表中删除
	 * @return 从工作表中取出的元素，如果工作表中没有元素则返回<code>null</code>
	 */
	public E pop() {
		E e = queue.poll();
		belong.remove(e);
		return e;
	}

	/**
	 * 清空工作表中所有元素
	 */
	public void clear() {
		queue.clear();
		belong.clear();
	}

	/**
	 * 判断工作表是否为空
	 * @return 工作表是否为空
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * 向工作表中加入多个元素
	 * @param ite 需要加入工作表元素的<code>Itearble</code>
	 */
	public void addAll(Iterable<E> ite) {
		ite.forEach(this::push);
	}

}
