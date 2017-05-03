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
				//�����ͷź���Ҫ����֪ͨ���������ߣ����ӳ��й黹��һ������
				pool.addLast(connection);
				pool.notifyAll();
			}
		}
	}
	//��ʱ�ȴ�����mill���޷���ȡ���ӣ����᷵��null
	public Connection fetchConnection(long mills) throws InterruptedException {
		synchronized (pool) {
			//��ȫ��ʱ
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
