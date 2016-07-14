package com.rabduck.mutter;


import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

public class ErrorDialog {

	public static void showErrorDialog(String msg, String detail, boolean runLater){
		if(runLater){
			Platform.runLater(() -> {
				showDialogMain(msg, detail);
			});
		}
		else{
			showDialogMain(msg, detail);
		}
	}
	
	public static void showErrorDialog(String msg, Exception e, boolean runLater){
		if(msg == null || msg.equals("")){
			msg = "Exception occured:";
		}
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		showErrorDialog(msg, errors.toString(), runLater);
	}
	
	private static void showDialogMain(String msg, String detail){
		Alert alert = new Alert(AlertType.ERROR);
		// prepare expandable content
		TextArea textArea = new TextArea(detail);
		alert.getDialogPane().setExpandableContent(textArea);
		 
		alert.setTitle("ERROR");
		alert.setHeaderText("Application Error");
		alert.setContentText(msg);
		alert.showAndWait();	
	}
}
