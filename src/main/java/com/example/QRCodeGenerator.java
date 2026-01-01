package com.example;

import com.formdev.flatlaf.FlatDarkLaf;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QRCodeGenerator extends JFrame {
	// UI Constants
	private static final int WINDOW_WIDTH = 650;
	private static final int WINDOW_HEIGHT = 850;
	private static final int MIN_WINDOW_WIDTH = 550;
	private static final int MIN_WINDOW_HEIGHT = 700;
	private static final int QR_CODE_SIZE = 300;
	private static final int QR_DISPLAY_PREFERRED = 280;
	private static final int QR_DISPLAY_MAX = 300;

	// Color Constants
	private static final Color GREEN_ACCENT = new Color(76, 175, 80);
	private static final Color DARK_BG = new Color(30, 30, 30);
	private static final Color PANEL_BG = new Color(40, 40, 40);
	private static final Color INPUT_BG = new Color(50, 50, 50);
	private static final Color BUTTON_BG = new Color(60, 60, 60);
	private static final Color BUTTON_HOVER_BG = new Color(70, 70, 70);
	private static final Color WARNING_COLOR = new Color(255, 150, 100);
	private static final Color ERROR_COLOR = new Color(255, 100, 100);

	// Font Constants
	private static final String FONT_NAME = "Segoe UI";
	private static final int TITLE_FONT_SIZE = 26;
	private static final int DEFAULT_FONT_SIZE = 12;

	// File Constants
	private static final String ICON_PATH = "src/main/resources/app-icon.png";
	private static final String DUMMY_QR_TEXT = "Insert a text";
	private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
	private static final String FILE_PREFIX = "QRCode_";
	private static final String FILE_EXTENSION = ".png";
	private static final String IMAGE_FORMAT = "png";

	// UI Components
	private JTextField inputField;
	private JLabel qrLabel;
	private JLabel statusLabel;

	// State
	private BufferedImage currentQRImage;
	private Color qrForegroundColor = Color.BLACK;
	private Color qrBackgroundColor = Color.WHITE;
	private File lastUsedDirectory = new File(System.getProperty("user.home", "."));

	public QRCodeGenerator() {
		setTitle("QR Code Generator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
		setLocationRelativeTo(null);
		setResizable(true);

		loadApplicationIcon();
		setupLookAndFeel();
		initComponents();
		generateDummyQRCode(DUMMY_QR_TEXT);
	}

	private void loadApplicationIcon() {
		try {
			ImageIcon icon = new ImageIcon(ICON_PATH);
			if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
				setIconImage(icon.getImage());
			}
		} catch (Exception e) {
			// Icon loading is optional, continue without it
		}
	}

	private void setupLookAndFeel() {
		try {
			UIManager.setLookAndFeel(new FlatDarkLaf());
		} catch (UnsupportedLookAndFeelException e) {
			// Fall back to default look and feel
			System.err.println("Could not set FlatDarkLaf: " + e.getMessage());
		}
	}

	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBackground(DARK_BG);
		mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

		// Title
		JLabel titleLabel = createLabel("QR Code Generator", Color.WHITE, Font.BOLD, TITLE_FONT_SIZE);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(titleLabel);
		mainPanel.add(Box.createVerticalStrut(20));

		// Input section
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		inputPanel.setBackground(PANEL_BG);
		inputPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
		inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

		JLabel inputLabel = createLabel("Enter text or URL:", Color.LIGHT_GRAY, Font.PLAIN, 13);
		inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		inputPanel.add(inputLabel);
		inputPanel.add(Box.createVerticalStrut(6));

		inputField = new JTextField();
		inputField.setFont(new Font(FONT_NAME, Font.PLAIN, DEFAULT_FONT_SIZE));
		inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
		inputField.setBackground(INPUT_BG);
		inputField.setForeground(Color.WHITE);
		inputField.setCaretColor(GREEN_ACCENT);
		inputField.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
		inputField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyReleased(java.awt.event.KeyEvent evt) {
				if (!inputField.getText().isEmpty()) {
					generateQRCode();
				}
			}
		});
		inputPanel.add(inputField);
		mainPanel.add(inputPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// Color customization panel
		JPanel colorPanel = new JPanel();
		colorPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		colorPanel.setBackground(DARK_BG);
		colorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

		JLabel customLabel = createLabel("Customize:", Color.LIGHT_GRAY, Font.PLAIN, DEFAULT_FONT_SIZE);
		colorPanel.add(customLabel);

		JButton fgColorBtn = createColorButton("QR Color", qrForegroundColor, true);
		JButton bgColorBtn = createColorButton("Background", qrBackgroundColor, false);
		colorPanel.add(fgColorBtn);
		colorPanel.add(bgColorBtn);
		mainPanel.add(colorPanel);
		mainPanel.add(Box.createVerticalStrut(12));

		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 0));
		buttonPanel.setBackground(DARK_BG);
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

		JButton generateBtn = createStyledButton("Generate", e -> generateQRCode());
		JButton copyBtn = createStyledButton("Copy", e -> copyToClipboard());
		JButton saveBtn = createStyledButton("Save", e -> saveQRCode());
		JButton clearBtn = createStyledButton("Clear", e -> clearAll());

		buttonPanel.add(generateBtn);
		buttonPanel.add(copyBtn);
		buttonPanel.add(saveBtn);
		buttonPanel.add(clearBtn);
		mainPanel.add(buttonPanel);
		mainPanel.add(Box.createVerticalStrut(15));

		// QR Code display
		qrLabel = new JLabel();
		qrLabel.setHorizontalAlignment(JLabel.CENTER);
		qrLabel.setVerticalAlignment(JLabel.CENTER);
		qrLabel.setPreferredSize(new Dimension(QR_DISPLAY_PREFERRED, QR_DISPLAY_PREFERRED));
		qrLabel.setMaximumSize(new Dimension(QR_DISPLAY_MAX, QR_DISPLAY_MAX));
		qrLabel.setBackground(PANEL_BG);
		qrLabel.setOpaque(true);
		qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.add(qrLabel);
		mainPanel.add(Box.createVerticalStrut(12));

		// Status area
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
		statusPanel.setBackground(DARK_BG);
		statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

		statusLabel = createLabel("Ready", Color.GRAY, Font.PLAIN, 11);
		statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		statusPanel.add(statusLabel);

		mainPanel.add(statusPanel);

		// Scroll pane
		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setBackground(DARK_BG);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		add(scrollPane);
	}

	private JButton createStyledButton(String text, java.awt.event.ActionListener action) {
		JButton btn = new JButton(text);
		btn.setFont(new Font(FONT_NAME, Font.PLAIN, 11));
		btn.setPreferredSize(new Dimension(85, 35));
		btn.setBackground(BUTTON_BG);
		btn.setForeground(Color.WHITE);
		btn.setBorder(new RoundedBorder(8, DARK_BG, 1));
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent e) {
				btn.setBorder(new RoundedBorder(8, GREEN_ACCENT, 1));
				btn.setBackground(BUTTON_HOVER_BG);
			}

			public void mouseExited(java.awt.event.MouseEvent e) {
				btn.setBackground(BUTTON_BG);
				btn.setBorder(new RoundedBorder(8, DARK_BG, 1));
			}
		});
		btn.addActionListener(action);
		return btn;
	}

	private JLabel createLabel(String text, Color foreground, int fontStyle, int fontSize) {
		JLabel label = new JLabel(text);
		label.setFont(new Font(FONT_NAME, fontStyle, fontSize));
		label.setForeground(foreground);
		return label;
	}

	private JButton createColorButton(String label, Color initialColor, boolean isForeground) {
		JButton btn = new JButton(label);
		btn.setFont(new Font(FONT_NAME, Font.PLAIN, 10));
		btn.setPreferredSize(new Dimension(100, 30));
		btn.setBackground(initialColor);
		btn.setForeground(getContrastColor(initialColor));
		btn.setFocusPainted(false);
		btn.setBorder(new RoundedBorder(6, initialColor, 1));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.addActionListener(e -> {
			Color currentColor = isForeground ? qrForegroundColor : qrBackgroundColor;
			Color selectedColor = JColorChooser.showDialog(this, "Choose " + label, currentColor);
			if (selectedColor != null) {
				if (isForeground) {
					qrForegroundColor = selectedColor;
				} else {
					qrBackgroundColor = selectedColor;
				}
				btn.setBackground(selectedColor);
				btn.setForeground(getContrastColor(selectedColor));
				btn.setBorder(new RoundedBorder(6, selectedColor, 1));
				if (currentQRImage != null) {
					generateQRCode();
				}
			}
		});
		return btn;
	}

	private Color getContrastColor(Color color) {
		// Calculate luminance to determine if text should be black or white
		double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
		return luminance > 0.5 ? Color.BLACK : Color.WHITE;
	}

	private void generateDummyQRCode(String text) {
		generateQRCodeInternal(text, false);
	}

	private void generateQRCode() {
		String text = inputField.getText().trim();

		if (text.isEmpty()) {
			updateStatus("⚠ Please enter text or URL", WARNING_COLOR);
			return;
		}

		generateQRCodeInternal(text, true);
	}

	private void generateQRCodeInternal(String text, boolean showStatus) {
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
			currentQRImage = createColoredQRCode(bitMatrix);

			ImageIcon icon = new ImageIcon(currentQRImage);
			qrLabel.setIcon(icon);

			if (showStatus) {
				updateStatus("✓ QR Code generated", GREEN_ACCENT);
			}
		} catch (WriterException e) {
			if (showStatus) {
				updateStatus("✗ Could not generate QR code", ERROR_COLOR);
			}
		} catch (IllegalArgumentException e) {
			if (showStatus) {
				updateStatus("✗ Content too large for QR code", ERROR_COLOR);
			}
		}
	}

	private BufferedImage createColoredQRCode(BitMatrix bitMatrix) {
		int width = bitMatrix.getWidth();
		int height = bitMatrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, bitMatrix.get(x, y) ? qrForegroundColor.getRGB() : qrBackgroundColor.getRGB());
			}
		}
		return image;
	}

	private void updateStatus(String message, Color color) {
		statusLabel.setText(message);
		statusLabel.setForeground(color);
	}

	private void copyToClipboard() {
		if (currentQRImage == null) {
			updateStatus("⚠ Generate a QR code first", WARNING_COLOR);
			return;
		}

		try {
			ImageSelection imageSelection = new ImageSelection(currentQRImage);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imageSelection, null);
			updateStatus("✓Copied to clipboard", GREEN_ACCENT);
		} catch (Exception e) {
			updateStatus("✗ Could not copy to clipboard", ERROR_COLOR);
		}
	}

	private void saveQRCode() {
		if (currentQRImage == null) {
			updateStatus("Generate a QR code first", WARNING_COLOR);
			return;
		}

		// Generate default filename with timestamp
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
		String defaultFileName = FILE_PREFIX + timestamp + FILE_EXTENSION;

		// Create file chooser with save dialog
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Save QR Code As...");
		fileChooser.setSelectedFile(new File(lastUsedDirectory, defaultFileName));

		// Set file filter to show only PNG files
		javax.swing.filechooser.FileNameExtensionFilter pngFilter = new javax.swing.filechooser.FileNameExtensionFilter(
				"PNG Images (*.png)", "png");
		fileChooser.setFileFilter(pngFilter);
		fileChooser.setAcceptAllFileFilterUsed(false);

		int result = fileChooser.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();

			// Ensure .png extension
			String filePath = selectedFile.getAbsolutePath();
			if (!filePath.toLowerCase().endsWith(FILE_EXTENSION)) {
				selectedFile = new File(filePath + FILE_EXTENSION);
			}

			// Update last used directory for next time
			lastUsedDirectory = selectedFile.getParentFile();
			if (lastUsedDirectory != null && lastUsedDirectory.exists()) {
				System.setProperty("user.dir", lastUsedDirectory.getAbsolutePath());
			}

			try {
				// Check if file exists and prompt for overwrite
				if (selectedFile.exists()) {
					int overwrite = JOptionPane.showConfirmDialog(this, "File already exists. Do you want to overwrite it?",
							"File Exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

					if (overwrite != JOptionPane.YES_OPTION) {
						return; // User cancelled
					}
				}

				javax.imageio.ImageIO.write(currentQRImage, IMAGE_FORMAT, selectedFile);
				updateStatus("Saved: " + selectedFile.getName(), GREEN_ACCENT);
			} catch (java.io.IOException e) {
				updateStatus("✗ Could not save image: " + e.getMessage(), ERROR_COLOR);
			} catch (Exception e) {
				updateStatus("✗ Could not save image", ERROR_COLOR);
			}
		}
	}

	private void clearAll() {
		inputField.setText("");
		qrLabel.setIcon(null);
		currentQRImage = null;
		updateStatus("Ready", Color.GRAY);
		generateDummyQRCode(DUMMY_QR_TEXT);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			QRCodeGenerator frame = new QRCodeGenerator();
			frame.setVisible(true);
		});
	}

	static class ImageSelection implements java.awt.datatransfer.Transferable {
		private static final java.awt.datatransfer.DataFlavor[] FLAVORS = {
				java.awt.datatransfer.DataFlavor.imageFlavor };
		private java.awt.image.BufferedImage image;

		public ImageSelection(java.awt.image.BufferedImage image) {
			this.image = image;
		}

		public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
			return FLAVORS;
		}

		public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
			return java.awt.datatransfer.DataFlavor.imageFlavor.equals(flavor);
		}

		public Object getTransferData(java.awt.datatransfer.DataFlavor flavor)
				throws java.awt.datatransfer.UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor)) {
				throw new java.awt.datatransfer.UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}

	// Custom rounded border class
	static class RoundedBorder extends javax.swing.border.AbstractBorder {
		private int radius;
		private Color color;
		private int thickness;

		public RoundedBorder(int radius, Color color, int thickness) {
			this.radius = radius;
			this.color = color;
			this.thickness = thickness;
		}

		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(color);
			g2d.setStroke(new BasicStroke(thickness));
			g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
		}

		public Insets getBorderInsets(Component c) {
			return new Insets(thickness, thickness, thickness, thickness);
		}
	}
}