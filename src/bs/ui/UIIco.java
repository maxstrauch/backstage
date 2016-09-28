package bs.ui;

import java.net.URL;

import javax.swing.ImageIcon;

public class UIIco extends ImageIcon {

	private static final long serialVersionUID = 1L;
	private static final String iconPath = "/res/";
	private static final String iconPathBs = "/res/backstage/";
	
	
	
	public static final UIIco bsAboutBackground = new UIIco(UIIco.class.getResource(
			iconPathBs + "bs-about.png"));
	
	public static final UIIco bsFrameIcon = new UIIco(UIIco.class.getResource(
			iconPathBs + "bs-logo-nos.png"));
	
	public static final UIIco bsStartUpBackground = new UIIco(UIIco.class.getResource(
			iconPathBs + "bs-startup.png"));
	
	
	
	
	
	
	
	
	
	
	
	public static final UIIco BS = new UIIco(UIIco.class.getResource(
			iconPath + "backstage-logo.png"));
	
	public static final UIIco LABEL = new UIIco(UIIco.class.getResource(
			iconPath + "labelformat.png"));
	
	
	
	public static final UIIco DOC_OPEN_16 = new UIIco(UIIco.class.getResource(
			iconPath + "document-open-16.png"));
	public static final UIIco DOC_OPEN_24 = new UIIco(UIIco.class.getResource(
			iconPath + "document-open-24.png"));
	
	
	public static final UIIco DOC_NEW_16 = new UIIco(UIIco.class.getResource(
			iconPath + "document-new-16.png"));
	
	
	public static final UIIco DOC_SAV_24 = new UIIco(UIIco.class.getResource(
			iconPath + "document-save-24.png"));
	public static final UIIco DOC_SAV_16 = new UIIco(UIIco.class.getResource(
			iconPath + "document-save-16.png"));
	
	
	
	
	public static final UIIco MEDIA_CD_24 = new UIIco(UIIco.class.getResource(
			iconPath + "media-cdrom-24.png"));
	public static final UIIco MEDIA_CD_16 = new UIIco(UIIco.class.getResource(
			iconPath + "media-cdrom-16.png"));
	public static final UIIco MEDIA_INV_24 = new UIIco(UIIco.class.getResource(
			iconPath + "media-inventory-24.png"));
	public static final UIIco MEDIA_INV_16 = new UIIco(UIIco.class.getResource(
			iconPath + "media-inventory-16.png"));
	public static final UIIco MEDIA_MUS_24 = new UIIco(UIIco.class.getResource(
			iconPath + "media-music-24.png"));
	public static final UIIco MEDIA_MUS_16 = new UIIco(UIIco.class.getResource(
			iconPath + "media-music-16.png"));
	
	
	
	public static final UIIco EDIT_24 = new UIIco(UIIco.class.getResource(
			iconPath + "edit-24.png"));
	public static final UIIco EDIT_16 = new UIIco(UIIco.class.getResource(
			iconPath + "edit-16.png"));
	public static final UIIco LIST_ADD_24 = new UIIco(UIIco.class.getResource(
			iconPath + "list-add-24.png"));
	public static final UIIco LIST_ADD_16 = new UIIco(UIIco.class.getResource(
			iconPath + "list-add-16.png"));
	public static final UIIco LIST_REMOVE_16 = new UIIco(UIIco.class.getResource(
			iconPath + "list-remove-16.png"));
	
	
	
	public static final UIIco EDIT_FIND_16 = new UIIco(UIIco.class.getResource(
			iconPath + "edit-find-16.png"));
	public static final UIIco EDIT_FIND_24 = new UIIco(UIIco.class.getResource(
			iconPath + "edit-find-24.png"));
	public static final UIIco PROCESS_STOP_16 = new UIIco(UIIco.class.getResource(
			iconPath + "process-stop-16.png"));
	public static final UIIco PROCESS_STOP_24 = new UIIco(UIIco.class.getResource(
			iconPath + "process-stop-24.png"));
	
	public static final UIIco DOC_PRINT_16 = new UIIco(UIIco.class.getResource(
			iconPath + "document-print-16.png"));
	public static final UIIco DOC_PRINT_48 = new UIIco(UIIco.class.getResource(
			iconPath + "document-print-48.png"));
	
	public static final UIIco APP_EXIT_16 = new UIIco(UIIco.class.getResource(
			iconPath + "application-exit-16.png"));
	
	public static final UIIco INFO_16 = new UIIco(UIIco.class.getResource(
			iconPath + "info-16.png"));
	
	public static final UIIco WEB_16 = new UIIco(UIIco.class.getResource(
			iconPath + "web-16.png"));
	
	
	public static final UIIco SORT_ASC_16 = new UIIco(UIIco.class.getResource(
			iconPath + "view-sort-ascending-16.png"));
	public static final UIIco SORT_DESC_16 = new UIIco(UIIco.class.getResource(
			iconPath + "view-sort-descending-16.png"));
	
	
	public static final UIIco REDO_16 = new UIIco(UIIco.class.getResource(
			iconPath + "edit-redo-16.png"));
	public static final UIIco UNDO_16 = new UIIco(UIIco.class.getResource(
			iconPath + "edit-undo-16.png"));
	
	public static final UIIco CLEAR_16 = new UIIco(UIIco.class.getResource(
			iconPath + "clear-16.png"));
	
	public static final UIIco GO_UP_16 = new UIIco(UIIco.class.getResource(
			iconPath + "go-up-16.png"));
	public static final UIIco GO_DOWN_16 = new UIIco(UIIco.class.getResource(
			iconPath + "go-down-16.png"));
	
	
	public UIIco(URL url) {
		super(url);
	}
	
}
