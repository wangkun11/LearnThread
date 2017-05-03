package ConnectionPool;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolTest {
	static ConnectionPool pool = new ConnectionPool(10);
	//��֤����ConnectionRunner�ܹ�ͬʱ��ʼ
	static CountDownLatch start =new CountDownLatch(1);
	//main�̵߳ȴ�����ConnectionRunner�������ִ��
	static CountDownLatch end;
	public static void main(String[] args) throws Exception {
		int threadCount=50;
		end=new CountDownLatch(threadCount);
		int count = 20;
		AtomicInteger got=new AtomicInteger();
		AtomicInteger notGot=new AtomicInteger();
		for (int i = 0; i < threadCount; i++) {
			Thread thread=new Thread(new ConnectionRunner(count, got, notGot),"ConnectionRunnerThread");
			thread.start();
		}
		start.countDown();//��2�������߳�ͬʱ�����������߳�
		end.await();//(3)�����߳���ͣ���ȴ��������߳�ִ����
		System.out.println("total invoke: "+(threadCount*count));
		System.out.println("got connection: "+got);
		System.out.println("not got connection: "+notGot);
	}
	static class ConnectionRunner implements Runnable{
		private int count;
		private AtomicInteger got;
		private AtomicInteger norGot;
		public ConnectionRunner(int count,AtomicInteger got,AtomicInteger notGot) {
			this.count=count;
			this.got=got;
			this.norGot=notGot;
		}
		@Override
		public void run() {
			try {
				start.await();//��1�����������̴߳���������ͣ���ȴ����̻߳���
			} catch (Exception e) {
			}
			while (count>0) {
				try {
					//
					Connection connection=pool.fetchConnection(1000);
					if (connection!=null) {
						try {
							connection.createStatement();
							connection.commit();
						} finally {
							pool.releaseConnection(connection);
							got.incrementAndGet();
						}
					}else {
						norGot.incrementAndGet();
					}
				} catch (Exception e) {
					// TODO: handle exception
				}finally{
					count--;
				}
			}
			end.countDown();//��4��ÿһ�����߳�ִ���꣬��ʹend��һ�����������߳̾�ִ�����ǣ�endΪ0�����̱߳�����
		}	
	}	
}
