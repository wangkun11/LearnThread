package ConnectionPool;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolTest {
	static ConnectionPool pool = new ConnectionPool(10);
	//保证所有ConnectionRunner能够同时开始
	static CountDownLatch start =new CountDownLatch(1);
	//main线程等待所有ConnectionRunner结束后才执行
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
		start.countDown();//（2）、主线程同时唤醒所以子线程
		end.await();//(3)、主线程暂停，等待所有子线程执行完
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
				start.await();//（1）、所以子线程创建后都先暂停，等待主线程唤醒
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
			end.countDown();//（4）每一个子线程执行完，都使end减一，当所以子线程均执行完是，end为0，主线程被唤醒
		}	
	}	
}
