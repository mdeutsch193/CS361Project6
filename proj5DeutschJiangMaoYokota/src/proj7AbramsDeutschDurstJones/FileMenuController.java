/*
 * File: FileMenuController.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file contains the FileMenuController class, handling File menu related
 * actions.
 */

package proj7AbramsDeutschDurstJones;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.richtext.CodeArea;

/**
 * FileMenuController handles File menu related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class FileMenuController {
  /**
   * Controller for the directory
   */
  private DirectoryController directoryController;
  /**
   * a HashMap mapping the tabs and the associated files
   */
  private Map<Tab, File> tabFileMap;
  /**
   * TabPane defined in Main.fxml
   */
  private CodeAreaTabPane codeAreaTabPane;

  /**
   * Sets the directory controller.
   *
   * @param directoryController Controller for the directory created in Controller
   */
  public void setDirectoryController(DirectoryController directoryController) {
    this.directoryController = directoryController;
  }

  /**
   * Sets the tabFileMap.
   *
   * @param tabFileMap HashMap mapping the tabs and the associated files
   */
  public void setTabFileMap(Map<Tab, File> tabFileMap) {
    this.tabFileMap = tabFileMap;
  }

  /**
   * Sets the codeAreaTabPane.
   *
   * @param codeAreaTabPane TabPane
   */
  public void setCodeAreaTabPane(CodeAreaTabPane codeAreaTabPane) { this.codeAreaTabPane = codeAreaTabPane; }

  /**
   * Helper method to get the text content of a specified file.
   *
   * @param file File to get the text content from
   * @return the text content of the specified file; null if an error occurs
   * when reading the specified file.
   */
  private String getFileContents(File file) {
    try {
      return new String(Files.readAllBytes(Paths.get(file.toURI())));
    } catch (Exception ex) {
      this.createErrorDialog("Reading File",
                             "Cannot read " + file.getName() + ".");
      return null;
    }
  }

  /**
   * Helper method to save the input string to a specified file.
   *
   * @param content String that is saved to the specified file
   * @param file File that the input string is saved to
   * @return true is the specified file is successfully saved; false if an error
   * occurs when saving the specified file.
   */
  public boolean setFileContents(String content, File file) {
    try {
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.write(content);
      fileWriter.close();
      return true;
    } catch (IOException ex) {
      this.createErrorDialog("Saving File",
                             "Cannot save to " + file.getName() + ".");
      return false;
    }
  }

  /**
   * Helper method to check if the content of the specified StyledJavaCodeArea
   * matches the content of the specified File.
   *
   * @param styledCodeArea StyledJavaCodeArea to compare with the the specified
   * File
   * @param file File to compare with the the specified StyledJavaCodeArea
   * @return true if the content of the StyledJavaCodeArea matches the content
   * of the File; false if not
   */
  public boolean fileContainsMatch(StyledJavaCodeArea styledCodeArea,
                                   File file) {
    String styledCodeAreaContent = styledCodeArea.getText();
    String fileContent = this.getFileContents(file);
    return styledCodeAreaContent.equals(fileContent);
  }

  /**
   * Helper method to handle closing tag action.
   * Checks if the text content within the specified tab window should be saved.
   *
   * @param tab Tab to be closed
   * @param ifSaveEmptyFile boolean false if not to save the empty file; true if
   * to save the empty file
   * @return true if the tab needs saving; false if the tab does not need
   * saving.
   */
  public boolean tabNeedsSaving(Tab tab, boolean ifSaveEmptyFile) {
    StyledJavaCodeArea activeStyledCodeArea =
        (StyledJavaCodeArea)((VirtualizedScrollPane)tab.getContent())
            .getContent();
    // check whether the embedded text has been saved or not
    if (this.tabFileMap.get(tab) == null) {
      // if the newly created file is empty, don't save
      if (!ifSaveEmptyFile) {
        return !activeStyledCodeArea.getText().equals("");
      }
      return true;
    }
    // check whether the saved file match the tab content or not
    else {
      return !this.fileContainsMatch(activeStyledCodeArea,
                                     this.tabFileMap.get(tab));
    }
  }

  /**
   * Helper method to handle closing tag action.
   * Removed the tab from the tab file mapping and from the TabPane.
   *
   * @param tab Tab to be closed
   */
  private void removeTab(Tab tab) {
    this.tabFileMap.remove(tab);
    this.codeAreaTabPane.getTabs().remove(tab);
  }

  /**
   * Checks whether a file embedded in the specified tab should be saved before
   * compiling. Pops up a dialog asking whether the user wants to save the file
   * before compiling. Saves the file if the user agrees so.
   *
   * @return 0 if user clicked NO button; 1 if user clicked OK button;
   *         2 is user clicked Cancel button; -1 is no saving is needed
   */
  public int checkSaveBeforeCompile() {
    Tab selectedTab = this.codeAreaTabPane.getSelectionModel().getSelectedItem();
    // if the file has not been saved or has been changed
    if (this.tabNeedsSaving(selectedTab, true)) {
      int buttonClicked = this.createConfirmationDialog(
          "Save Changes?", "Do you want to save the changes before compiling?",
          "Your recent file changes would not be compiled if not saved.");
      // if user presses Yes button
      if (buttonClicked == 1) {
        this.handleSaveAction();
      }
      return buttonClicked;
    }
    return -1;
  }

  /**
   * Helper method to create a confirmation dialog window.
   *
   * @param title the title of the confirmation dialog
   * @param headerText the header text of the confirmation dialog
   * @param contentText the content text of the confirmation dialog
   * @return 0 if the user clicks No button; 1 if the user clicks the Yes
   * button; 2 if the user clicks cancel button.
   */
  public int createConfirmationDialog(String title, String headerText,
                                      String contentText) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(headerText);
    alert.setContentText(contentText);

    ButtonType buttonYes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
    ButtonType buttonNo = new ButtonType("No", ButtonBar.ButtonData.NO);
    ButtonType buttonCancel =
        new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(buttonYes, buttonNo, buttonCancel);

    Optional<ButtonType> result = alert.showAndWait();

    if (result.get() == buttonNo) {
      return 0;
    } else if (result.get() == buttonYes) {
      return 1;
    } else {
      return 2;
    }
  }

  /**
   * Helper method to create a new tab.
   *
   * @param contentString the contentString being added into the styled code
   * area; empty string if creating an empty window
   * @param filename the name of the file opened; "untitled" if creating an
   * empty window
   * @param file File opened; null if creating an empty window
   */
  private void createTab(String contentString, String filename, File file) {
    StyledJavaCodeArea newStyledCodeArea =
        new StyledJavaCodeArea(contentString);

    Tab newTab = new Tab();
    newTab.setText(filename);
    newTab.setContent(new VirtualizedScrollPane<>(newStyledCodeArea));
    newTab.setOnCloseRequest(event -> this.handleCloseAction(event));

    this.tabFileMap.put(newTab, file);
    this.codeAreaTabPane.getTabs().add(newTab);
    this.codeAreaTabPane.getSelectionModel().select(newTab);
  }

  /**
   * Helper method to handle closing tag action.
   * If the text embedded in the tab window has not been saved yet,
   * or if a saved file has been changed, asks the user if to save
   * the file via a dialog window.
   *
   * @param tab Tab to be closed
   * @return true if the tab is closed successfully; false if the user clicks
   * cancel.
   */
  private boolean closeTab(Tab tab) {
    // if the file has not been saved or has been changed
    // pop up a dialog window asking whether to save the file
    if (this.tabNeedsSaving(tab, false)) {
      int buttonClicked = this.createConfirmationDialog(
          "Save Changes?", "Do you want to save the changes you made?",
          "Your changes will be lost if you don't save them.");

      // if user presses No button, close the tab without saving
      if (buttonClicked == 0) {
        this.removeTab(tab);
        return true;
      }
      // if user presses Yes button, close the tab and save the tab content
      else if (buttonClicked == 1) {
        if (this.handleSaveAction()) {
          this.removeTab(tab);
          return true;
        }
        return false;
      }
      // if user presses cancel button
      else {
        return false;
      }
    }
    // if the file has not been changed, close the tab
    else {
      this.removeTab(tab);
      return true;
    }
  }

  /**
   * Creates a error dialog displaying message of any error encountered.
   *
   * @param errorTitle String of the error title
   * @param errorString String of error message
   */
  public void createErrorDialog(String errorTitle, String errorString) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(errorTitle + " Error");
    alert.setHeaderText("Error for " + errorTitle);
    alert.setContentText(errorString);
    alert.showAndWait();
  }

  /**
   * Handles the New button action.
   * Opens a styled code area embedded in a new tab.
   * Sets the newly opened tab to the the topmost one.
   */
  public void handleNewAction() { this.createTab("", "untitled", null); }

  /**
   * Handles the open button action.
   * Opens a dialog in which the user can select a file to open.
   * If the user chooses a valid file, a new tab is created and the file is
   * loaded into the styled code area. If the user cancels, the dialog
   * disappears without doing anything.
   */
  public void handleOpenAction() {
    FileChooser fileChooser = new FileChooser();
    File openFile =
        fileChooser.showOpenDialog(this.codeAreaTabPane.getScene().getWindow());
    this.handleOpenFile(openFile);
  }

  /**
   * Handles opening the given file object
   * @param file the File to open in a new tab
   */
  public void handleOpenFile(File file) {
    if (file == null) {
      return;
    }
    // if the selected file is already open, it cannot be opened twice
    // the tab containing this file becomes the current (topmost) one
    for (Map.Entry<Tab, File> entry : this.tabFileMap.entrySet()) {
      if (entry.getValue() != null) {
        if (entry.getValue().equals(file)) {
          this.codeAreaTabPane.getSelectionModel().select(entry.getKey());
          return;
        }
      }
    }
    String contentString = this.getFileContents(file);
    if (contentString == null) {
      return;
    }

    this.createTab(contentString, file.getName(), file);
  }

  /**
   * Handles the save button action.
   * If a styled code area was not loaded from a file nor ever saved to a file,
   * behaves the same as the save as button.
   * If the current styled code area was loaded from a file or previously saved
   * to a file, then the styled code area is saved to that file.
   *
   * @return true if save as successfully; false if cancels or an error occurs
   * when saving the file.
   */
  public boolean handleSaveAction() {
    // get the selected tab from the tab pane
    Tab selectedTab = this.codeAreaTabPane.getSelectionModel().getSelectedItem();

    // if the tab content was not loaded from a file nor ever saved to a file
    // save the content of the active styled code area to the selected file path
    if (this.tabFileMap.get(selectedTab) == null) {
      return this.handleSaveAsAction();
    }
    // if the current styled code area was loaded from a file or previously
    // saved to a file, then the styled code area is saved to that file
    else {
      CodeArea activeCodeArea = this.codeAreaTabPane.getActiveCodeArea();
      return this.setFileContents(activeCodeArea.getText(),
                                  this.tabFileMap.get(selectedTab));
    }
  }

  /**
   * Handles the Save As button action.
   * Shows a dialog in which the user is asked for the name of the file into
   * which the contents of the current styled code area are to be saved.
   * If the user enters any legal name for a file and presses the OK button in
   * the dialog, then creates a new text file by that name and write to that
   * file all the current contents of the styled code area so that those
   * contents can later be reloaded and opens the file directory.
   * If the user presses the Cancel button in the dialog, then the
   * dialog closes and no saving occurs.
   *
   * @return true if save as successfully; false if cancels or an error occurs
   * when saving the file.
   */
  public boolean handleSaveAsAction() {
    FileChooser fileChooser = new FileChooser();
    File saveFile =
        fileChooser.showSaveDialog(this.codeAreaTabPane.getScene().getWindow());

    if (saveFile == null) {
      return false;
    }
    // get the selected tab from the tab pane
    Tab selectedTab = this.codeAreaTabPane.getSelectionModel().getSelectedItem();
    CodeArea activeCodeArea = this.codeAreaTabPane.getActiveCodeArea();
    if (!this.setFileContents(activeCodeArea.getText(), saveFile)) {
      return false;
    }
    // set the title of the tab to the name of the saved file
    selectedTab.setText(saveFile.getName());

    // map the tab and the associated file
    this.tabFileMap.put(selectedTab, saveFile);

    // open file directory
    this.directoryController.createDirectoryTree();

    return true;
  }

  /**
   * Handles the close button action.
   * If the current styled code area has already been saved to a file, then the
   * current tab is closed. If the current styled code area has been changed
   * since it was last saved to a file, a dialog appears asking whether you want
   * to save the text before closing it.
   *
   * @param event Event object
   */
  public void handleCloseAction(Event event) {
    Tab selectedTab = this.codeAreaTabPane.getSelectionModel().getSelectedItem();

    // selectedTab is null if this method is evoked by closing a tab
    // in this case the selectedTab tab should be the tab that evokes this
    // method
    if (selectedTab == null) {
      selectedTab = (Tab)event.getSource();
    }
    // if the user select to not close the tab, then we consume the event (not
    // performing the closing action)
    if (!this.closeTab(selectedTab)) {
      event.consume();
    }
  }

  /**
   * Handles the Exit button action.
   * Exits the program when the Exit button is clicked.
   *
   * @param event Event object
   */
  public void handleExitAction(Event event) {
    ArrayList<Tab> tabList = new ArrayList<>(this.tabFileMap.keySet());
    for (Tab currentTab : tabList) {
      this.codeAreaTabPane.getSelectionModel().select(currentTab);
      if (!this.closeTab(currentTab)) {
        event.consume();
        return;
      }
    }
    System.exit(0);
  }

  /**
   * Handles the About button action.
   * Creates a dialog window that displays the authors' names.
   */
  public void handleAboutAction() {
    // create a information dialog window displaying the About text
    Alert dialog = new Alert(Alert.AlertType.INFORMATION);

    // enable to close the window by clicking on the red cross on the top left
    // corner of the window
    Window window = dialog.getDialogPane().getScene().getWindow();
    window.setOnCloseRequest(event -> window.hide());

    // set the title and the content of the About window
    dialog.setTitle("About");
    dialog.setHeaderText("Authors");
    dialog.setContentText(
        "---- Project 4 ---- \nLiwei Jiang\nDanqing Zhao\nWyett MacDonald\nZeb Keith-Hardy"
        +
        "\n\n---- Project 5 ---- \nLiwei Jiang\nMartin Deutsch\nMelody Mao\nTatsuya Yakota"
        +
        "\n\n---- Project 6 ---- \nDoug Abrams\nMartin Deutsch\nRob Durst\nMatt Jones");

    // enable to resize the About window
    dialog.setResizable(true);
    dialog.showAndWait();
  }
}