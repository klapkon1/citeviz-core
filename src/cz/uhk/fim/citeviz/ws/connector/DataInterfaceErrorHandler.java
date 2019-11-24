package cz.uhk.fim.citeviz.ws.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import cz.uhk.fim.citeviz.gui.components.Localizer;

public class DataInterfaceErrorHandler {

	private static final List<Throwable> lastErrors = new ArrayList<>();
	
	public static void storeError(Throwable e){
		lastErrors.add(e);
		e.printStackTrace();
	}
	
	public static void storeError(String errCode, String errMessage){
		storeError(new CommunicationException(errCode, errMessage));
	}
	
	public static void handleError(Throwable e){
		String title = null;
		String message = null;
		e.printStackTrace();
		
		if (e instanceof IOException){
			title = Localizer.getString("dataInterface.communicationError.title");
			message = Localizer.getString("dataInterface.communicationError", e.getLocalizedMessage());
		} else if (e instanceof ParserConfigurationException  || e instanceof SAXException){
			title = Localizer.getString("dataInterface.parseError.title");
			message = Localizer.getString("dataInterface.parseError", e.getLocalizedMessage());
		} else if (e instanceof CommunicationException){
			title = Localizer.getString("dataInterface.returnedError.title");
			CommunicationException communicationException = (CommunicationException) e;
			message = Localizer.getString("dataInterface.returnedError", communicationException.getErrCode(), communicationException.getErrMessage());
		} else {
			title = Localizer.getString("dataInterface.parseError.title");
			if (e.getLocalizedMessage() != null) {
				message = Localizer.getString("dataInterface.parseError", e.getLocalizedMessage());
			} else {
				message = Localizer.getString("dataInterface.parseError");
			}
			
		}
		
		
		final String messageForPrint = message;
		final String titleForPrint = title;
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, messageForPrint, titleForPrint, JOptionPane.ERROR_MESSAGE);
			}
		});
		
	}
	
	public static void handleLastErrors(){
		if (!lastErrors.isEmpty()) {
			handleError(lastErrors.get(lastErrors.size() - 1));
			lastErrors.clear();
		}
	}
	
	public static class CommunicationException extends Exception{
		private static final long serialVersionUID = 1L;
		
		private String errCode;
		private String errMessage;
		
		public CommunicationException(String errCode, String errMessage) {
			super();
			this.errCode = errCode;
			this.errMessage = errMessage;
		}
		
		public String getErrCode() {
			return errCode;
		}
		
		public String getErrMessage() {
			return errMessage;
		}
	}
}
