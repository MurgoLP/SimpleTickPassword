import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class SimpleTickPassword extends JFrame {
	public static final int MillisecondsPerTick = 1000;

	public static void main(String[] args) {
		new SimpleTickPassword();
	}

	private JButton OKbutton, RESETButton;
	private JTextField textField = new JTextField(15);
	private TimedKeyListener keyListener;
	private JLabel timerText = new JLabel();

	public SimpleTickPassword() {
		this.setLocation(700, 500);
		this.setAlwaysOnTop(true);
		this.setResizable(false);
		this.setSize(325, 100);
		this.setTitle("Simple Tick Password Protect");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		keyListener = new TimedKeyListener(textField, timerText);
		ButtonListener ButtonListener1 = new ButtonListener(keyListener);
		JPanel Canvas = new JPanel();

		Canvas.add(new JLabel("Password: "));
		Canvas.add(textField);
		OKbutton = new JButton("OK");
		OKbutton.addActionListener(ButtonListener1);
		RESETButton = new JButton("Reset");
		RESETButton.addActionListener(ButtonListener1);
		Canvas.add(RESETButton);
		Canvas.add(timerText);
		// timerText.setText("text");
		Canvas.add(OKbutton);
		this.add(Canvas);
		this.setVisible(true);
		textField.addKeyListener(keyListener);
		textField.setEditable(false);
	}

	public class TimedKeyListener implements KeyListener {
		Hashtable<Character, Long> startTimes = new Hashtable<Character, Long>();
		public ArrayList<PasswordEntry> PasswordEntries = new ArrayList<PasswordEntry>();
		private JTextField textField;
		private final int[] specialCharCodes = { KeyEvent.VK_BACK_QUOTE,
				KeyEvent.VK_MINUS, KeyEvent.VK_PLUS, KeyEvent.VK_UNDERSCORE,
				KeyEvent.VK_EQUALS, KeyEvent.VK_BACK_SLASH,
				KeyEvent.VK_BRACELEFT, KeyEvent.VK_BRACERIGHT,
				KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_SEMICOLON,
				KeyEvent.VK_COMMA, KeyEvent.VK_DECIMAL, KeyEvent.VK_DIVIDE,
				KeyEvent.VK_EQUALS, KeyEvent.VK_MULTIPLY, KeyEvent.VK_QUOTE,
				KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, KeyEvent.VK_SUBTRACT,
				KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_COLON, };
		private JLabel timerText;

		public TimedKeyListener(JTextField _textField, JLabel _timerText) {
			textField = _textField;
			timerText = _timerText;
		}

		public void keyPressed(KeyEvent e) {
			if (!shouldIgnoreKey(e.getExtendedKeyCode())) {
				if (startTimes.isEmpty()) {
					if (!startTimes.containsKey(e.getKeyChar())) {
						startTimes.put(e.getKeyChar(),
								System.currentTimeMillis());
						System.out.println(e.getKeyChar());
					}
				} else if (startTimes.containsKey(e.getKeyChar())) {
					long numberTicks = (System.currentTimeMillis() - startTimes
							.get(e.getKeyChar())) / MillisecondsPerTick;
					timerText.setText("Ticks: " + Long.toString(numberTicks));
				}
			}
		}

		public void keyReleased(KeyEvent e) {
			if (!shouldIgnoreKey(e.getExtendedKeyCode())
					&& startTimes.containsKey(e.getKeyChar())) {
				long endTime = System.currentTimeMillis();
				long startTime = startTimes.get(e.getKeyChar());
				long deltaTime = endTime - startTime;
				System.out.println(deltaTime);
				startTimes.remove(e.getKeyChar());
				PasswordEntries
						.add(new PasswordEntry(e.getKeyChar(), deltaTime));
				String s = textField.getText();
				s += e.getKeyChar();
				textField.setText(s);
				timerText.setText("");
			}
		}

		public void keyTyped(KeyEvent e) {
		}

		private boolean shouldIgnoreKey(int keyCode) {
			if ((keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9)
					|| (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)) {
				return false;
			}
			for (int i = 0; i < specialCharCodes.length; ++i) {
				if (keyCode == specialCharCodes[i]) {
					return false;
				}
			}
			return true;
		}
	}

	public class PasswordEntry {
		public char KeyChar;
		public long KeyMilliseconds;

		public PasswordEntry(char keyChar, long keyMilliseconds) {
			KeyChar = keyChar;
			KeyMilliseconds = keyMilliseconds;
		}
	}

	private class ButtonListener implements ActionListener {
		private TimedKeyListener keyListener;

		public ButtonListener(TimedKeyListener _keyListener) {
			keyListener = _keyListener;
		}

		public void actionPerformed(ActionEvent a) {
			if (a.getSource() == OKbutton) {
				String password = textField.getText();
				if (password.length() <= 3) {
					JOptionPane.showMessageDialog(SimpleTickPassword.this,
							"Enter a password with at least four characters",
							password, JOptionPane.INFORMATION_MESSAGE, null);
					resetField();
				} else {
					Path path = Paths.get("src/resources/db.properties");
					if (Files.exists(path)) {
						FileReader read = null;
						try {
							read = new FileReader("src/resources/db.properties");
							Properties properties = new Properties();
							properties.load(read);
							String pw = properties.getProperty("password");
							int[] charTimes = new int[pw.length()];
							for (int i = 0; i < charTimes.length; ++i) {
								String charKeyName = new String("char")
										+ Integer.toString(i);
								String value = properties
										.getProperty(charKeyName);
								charTimes[i] = Integer.parseInt(value);
							}
							if (pw.equals(password)) {
								for (int i = 0; i < password.length(); ++i) {
									if (charTimes[i] != keyListener.PasswordEntries
											.get(i).KeyMilliseconds
											/ MillisecondsPerTick) {
										JOptionPane.showMessageDialog(
												SimpleTickPassword.this,
												"Incorrect password");
										resetField();
										break;
									}
								}
								if (textField.getText().length() > 0) {
									JOptionPane.showMessageDialog(
											SimpleTickPassword.this,
											"Password correct!");
									System.exit(0);
								}
							} else {
								JOptionPane.showMessageDialog(
										SimpleTickPassword.this,
										"Incorrect password");
								resetField();
							}
						} catch (Exception b) {
							b.printStackTrace();
							JOptionPane.showMessageDialog(
									SimpleTickPassword.this,
									"Error reading password file : "
											+ b.getMessage());
						} finally {
							try {
								read.close();
							} catch (Exception d) {
								JOptionPane.showMessageDialog(
										SimpleTickPassword.this,
										"I couldn't close bd.properties");
							}
						}
					}
					if (Files.notExists(path)) {
						FileWriter write = null;
						try {
							write = new FileWriter(
									"src/resources/db.properties", false);
							Properties properties = new Properties();
							properties.setProperty("password",
									textField.getText());
							for (int i = 0; i < textField.getText().length(); ++i) {
								String charKeyName = new String("char")
										+ Integer.toString(i);
								int nTicks = (int) (keyListener.PasswordEntries
										.get(i).KeyMilliseconds / MillisecondsPerTick);
								properties.setProperty(charKeyName,
										Integer.toString(nTicks));
							}
							properties.store(write, "password and ticks");

							JOptionPane.showMessageDialog(
									SimpleTickPassword.this, "Welcome");
						} catch (Exception b) {
							b.printStackTrace();
							JOptionPane.showMessageDialog(
									SimpleTickPassword.this,
									"Error writing password file : "
											+ b.getMessage());
						} finally {
							try {
								write.close();
							} catch (Exception d) {
								JOptionPane.showMessageDialog(
										SimpleTickPassword.this,
										"I couldn't close db.properties");
							}
						}
					}
					textField.requestFocus();
				}
			} else if (a.getSource() == RESETButton) {
				resetField();
			}
		}

		private void resetField() {
			textField.setText(new String());
			keyListener.PasswordEntries.clear();
			timerText.setText("");
			keyListener.startTimes.clear();
		}
	}
}