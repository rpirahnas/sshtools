package com.sshtools.common.ui;

import javax.swing.Action;

public class SessionProviderAction
    extends StandardAction {

  SessionProvider provider;

  public SessionProviderAction(SessionProvider provider) {
    this.provider = provider;
    putValue(Action.NAME, provider.getName());
    putValue(Action.SMALL_ICON, provider.getSmallIcon());
    putValue(LARGE_ICON, provider.getLargeIcon());
    putValue(Action.SHORT_DESCRIPTION, provider.getDescription());
    putValue(Action.LONG_DESCRIPTION, provider.getDescription());
    putValue(Action.MNEMONIC_KEY, new Integer(provider.getMnemonic()));
    putValue(Action.ACTION_COMMAND_KEY, provider.getName());
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "Tools");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(10));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(70));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(40));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(20));

  }

  public SessionProvider getProvider() {
    return provider;
  }
}
