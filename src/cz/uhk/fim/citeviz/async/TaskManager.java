package cz.uhk.fim.citeviz.async;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import cz.uhk.fim.citeviz.gui.components.ButtonTableCellRenderer;
import cz.uhk.fim.citeviz.gui.components.Localizer;

public class TaskManager {
	
	private Map<String, AsyncTask<?>> runningTasks = new ConcurrentHashMap<String, AsyncTask<?>>();
	
	private static TaskManager instance;
	
	private JTable tblTasks = new JTable();
	
	private TaskManager() {
		updateModel();
	}
	
	public static TaskManager getInstance(){
		if (instance == null){
			instance = new TaskManager();
		}
		
		return instance;
	}
	
	public void startTask(AsyncTask<?> task){
		startTask(task, generateGroupId());
	}
	
	public void startTask(AsyncTask<?> task, String groupId){
		String taskId = task.startTask(groupId);
		runningTasks.put(taskId, task);
	}
	
	public String generateGroupId(){
		return UUID.randomUUID().toString();
	}
	
	public void taskFinished(String taskId){
		runningTasks.remove(taskId);
		updateModel();
	}
	
	public void stopTask(String taskId){
		if (runningTasks.containsKey(taskId)){
			runningTasks.get(taskId).stopTask();
		}
	}
	
	public List<AsyncTask<?>> getRunningTasksInGroup(String groupId) {
		List<AsyncTask<?>> tasks = new ArrayList<AsyncTask<?>>();
		for (AsyncTask<?> asyncTask : runningTasks.values()) {
			if (asyncTask.getGroupId() != null && asyncTask.getGroupId().equals(groupId) && asyncTask.isRunning()){
				tasks.add(asyncTask);
			}
		}
		
		return tasks;
	}
	
	public boolean isGroupRunnig(String groupId){
		for (AsyncTask<?> asyncTask : runningTasks.values()) {
			if (asyncTask.getGroupId() != null && asyncTask.getGroupId().equals(groupId) && asyncTask.isRunning()){
				return true;
			}
		}
		
		return false;
	}
	
	private void updateModel(){
		final List<AsyncTask<?>> tasks = new ArrayList<>(runningTasks.values()); 
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				tblTasks.setModel(new AbstractTableModel() {
					private static final long serialVersionUID = 1L;
		
					@Override
					public Object getValueAt(int rowIndex, int columnIndex) {
						switch (columnIndex){
							case 0: return tasks.get(rowIndex).getGroupId();
							case 1: return tasks.get(rowIndex).isRunning();
							case 2: return tasks.get(rowIndex).getRunningTime() + " " + Localizer.getString("taskManager.seconds");
							default : return null;
						}
					}
					
					@Override
					public String getColumnName(int column) {
						switch (column){
							case 0: return Localizer.getString("taskManager.taskName");
							case 1: return Localizer.getString("taskManager.taskStatus");
							case 2: return Localizer.getString("taskManager.runningTime");
							default: return null;
						}
					}
					
					@Override
					public int getRowCount() {
						return tasks.size();
					}
					
					@Override
					public int getColumnCount() {
						return 3;
					}
					
					@Override
					public boolean isCellEditable(int rowIndex, int columnIndex) {
						return columnIndex == 3;
					}
				});
				
				tblTasks.getColumnModel().getColumn(2).setCellRenderer(new ButtonTableCellRenderer());
				
				tblTasks.revalidate();
				tblTasks.repaint();
			}
		});
	}
	
	public Component getTasksView(){
		return new JScrollPane(tblTasks);
	}
}