package cz.uhk.fim.citeviz.async;

import java.util.UUID;

public abstract class AsyncTask<D> extends Thread{
	
	private boolean running = false;
	
	private String taskId =  UUID.randomUUID().toString();
	
	private String groupId;
	
	private long startTime;
	
	public boolean isRunning(){
		return running;
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	@Override
	public void run() {
		startTime = System.currentTimeMillis();
		running = true;
		try {
			D result = runTask();
			if (running){
				running = false;
				synchronized (getLockObject()){
					TaskManager.getInstance().taskFinished(taskId);
					onFinish(result);
				}
			}
		} catch (Throwable e){
			running = false;
			synchronized (getLockObject()){
				TaskManager.getInstance().taskFinished(taskId);
				onError(e);
			}
		}
	}
	
	protected abstract Object getLockObject();

	/**
	 * please check isRunning in suitable places, when false please stop working
	 * 
	 * @return
	 */
	protected abstract D runTask() throws Throwable;
	
	protected abstract void onFinish(D result);
	
	protected abstract void onError(Throwable e);
	
	public String startTask(String groupId){
		this.groupId = groupId;
		start();
		return taskId;
	}
	
	public void stopTask(){
		if (running){
			running = false;
		}
	}
	
	public String getTaskId() {
		return taskId;
	}
	
	public long getRunningTime(){
		return (System.currentTimeMillis() - startTime) / 1000;
	}
}