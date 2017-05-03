package ConnectionPool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class ConnectionPool {
	private LinkedList<Connection> pool=new LinkedList<Connection>();
	public ConnectionPool(int initialSize){
		if (initialSize>0) {
			for (int i = 0; i < initialSize; i++) {
				pool.addLast(ConnectionDriver.createConnection());
			}
		}
	}
	public void releaseConnection(Connection connection) {
		if (connection!=null) {
			synchronized (pool) {
				//连接释放后需要进行通知其它消费者：连接池中归还了一个连接
				pool.addLast(connection);
				pool.notifyAll();
			}
		}
	}
	//超时等待：在mill内无法获取连接，将会返回null
	public Connection fetchConnection(long mills) throws InterruptedException {
		synchronized (pool) {
			//完全超时
			if (mills<=0) {
				while (pool.isEmpty()) {
					pool.wait();					
				}
				return pool.removeFirst();
			}else {
				long future=System.currentTimeMillis()+mills;
				long remaining=mills;
				while (pool.isEmpty()&&remaining>0) {
					pool.wait(remaining);
					remaining=future-System.currentTimeMillis();
				}
				Connection result=null;
				if (!pool.isEmpty()) {
					result=pool.removeFirst();
				}
				return result;
			}
		}
	}
}
