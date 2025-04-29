package com.work.oblikcars.Utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class AutoCompleteComboBoxListener<T> implements EventHandler<KeyEvent> {
    private final ComboBox<T> comboBox;
    private final ObservableList<T> originalItems;
    private boolean moveCaretToPos = false;
    private int caretPos;

    public AutoCompleteComboBoxListener(ComboBox<T> comboBox) {
        this.comboBox = comboBox;
        this.comboBox.setEditable(true);
        this.originalItems = FXCollections.observableArrayList(comboBox.getItems());
        this.comboBox.getEditor().setOnKeyReleased(this);
    }

    @Override
    public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
            // дозволяємо навігацію стрілками
            if (!comboBox.isShowing()) comboBox.show();
            return;
        }
        if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
            moveCaretToPos = true;
            caretPos = comboBox.getEditor().getCaretPosition();
        }
        if (event.getCode().isArrowKey() ||
                event.getCode() == KeyCode.HOME ||
                event.getCode() == KeyCode.END ||
                event.getCode() == KeyCode.TAB) {
            return;
        }

        String text = comboBox.getEditor().getText();
        ObservableList<T> filtered = FXCollections.observableArrayList();
        for (T item : originalItems) {
            if (item.toString().toLowerCase().contains(text.toLowerCase())) {
                filtered.add(item);
            }
        }
        comboBox.setItems(filtered);
        comboBox.getEditor().setText(text);
        // рух курсору
        if (!moveCaretToPos) {
            caretPos = -1;
        }
        moveCaret(text.length());
        if (!filtered.isEmpty()) {
            comboBox.show();
        }
    }

    private void moveCaret(int textLength) {
        if (caretPos == -1) {
            comboBox.getEditor().positionCaret(textLength);
        } else {
            comboBox.getEditor().positionCaret(caretPos);
        }
        moveCaretToPos = false;
    }
}

