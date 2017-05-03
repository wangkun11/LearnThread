package ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {

	private static final int Max_Worker_Numbers=10;
	private static final int Default_Worker_Numbers=5;
	private static final int Min_Worker_Numbers=1;
	
	private final LinkedList<Job> jobs=new LinkedList<Job>();
	private final List<Worker> workers=Collections.synchronizedList(new ArrayList<Worker>());
	
	private int workerNum=Default_Worker_Numbers;
	private AtomicLong threadNum=new AtomicLong();
	
	
	public DefaultThreadPool() {
		initalizeWorkers(Default_Worker_Numbers);
	}
	public DefaultThreadPool(int num) {
		workerNum=(num>Max_Worker_Numbers?Max_Worker_Numbers:(num<Min_Worker_Numbers?Min_Worker_Numbers:num));
		initalizeWorkers(workerNum);
	}
	
	private void initalizeWorkers(int num){
		for (int i = 0; i < num; i++) {
			Worker worker=new Worker();
			workers.add(worker);
			Thread thread=new Thread(worker,"ThreadPool-Worker-"+threadNum.incrementAndGet());
			thread.start();
		}
	}
	@Override
	public void execute(Job job) {
		if (job!=null) {
			synchronized (jobs) {
				jobs.addLast(job);
				jobs.notify();
			}
		}
	}

	@Override
	public void shutdown() {
		for (Worker worker : workers) {
			worker.shutdown();
		}
	}

	@Override
	public void addWorkers(int num) {
		synchronized (jobs) {
			if (num+this.workerNum>Max_Worker_Numbers) {
				num=Max_Worker_Numbers-this.workerNum;
			}
			initalizeWorkers(num);
			this.workerNum+=num;
		}		
	}
	@Override
	public void removeWorkers(int num) {
		synchronized (jobs) {
			if (num>workerNum) {
				throw new IllegalArgumentException("beyond workNum");
			}
			int count=0;
			while (count<num) {
				Worker worker=workers.get(count);
				if (workers.remove(worker)) {
					worker.shutdown();
					count++;
				}
			}
			workerNum-=count;
		}
	}

	@Override
	public int getJobSize() {
		return jobs.size();
	}
	//工作者类，负责消费任务（job）
	class Worker implements Runnable{
		private volatile boolean running=true;
		@Override
		public void run() {
			while (running) {
				Job job=null;
				synchronized (jobs) {
					//如果工作列表是空的，就wait
					while (jobs.isEmpty()) {
						try {
							jobs.wait();
						} catch (InterruptedException e) {
							//感知到外部对WorkerThread的中断操作，返回
							Thread.currentThread().interrupt();
							return;
						}
					}
					job=jobs.removeFirst();
				}
				if (job!=null) {
					try {
						job.run();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		}
		public void shutdown() {
			running=false;
		}		
	}
}
