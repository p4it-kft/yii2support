package com.nvlad.yii2support.migrations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.AnActionButton;
import com.nvlad.yii2support.migrations.MigrationManager;
import com.nvlad.yii2support.migrations.entities.Migration;
import com.nvlad.yii2support.migrations.entities.MigrationStatus;
import com.nvlad.yii2support.migrations.ui.MigrationPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.Set;

@SuppressWarnings("ComponentNotRegistered")
public class MigrateUpAction extends AnActionButton {
    public MigrateUpAction() {
        super("Migrate Up", AllIcons.Actions.Execute);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof String) {
            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
            Set<String> migrations = manager.migrateUp((String) userObject, 0);
        }

        if (userObject instanceof Migration) {
            Migration migration = (Migration) userObject;
            if (migration.status == MigrationStatus.Success) {
                return;
            }

            int count = 0;
            Enumeration migrationEnumeration = treeNode.getParent().children();
            while (migrationEnumeration.hasMoreElements()) {
                Migration tmp = (Migration) ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (migration.status != MigrationStatus.Success) {
                    count++;
                }

                if (tmp == migration) {
                    break;
                }
            }

            if (count == 0) {
                return;
            }

            MigrationManager manager = MigrationManager.getInstance(anActionEvent.getProject());
            Object parentUserObject = ((DefaultMutableTreeNode) treeNode.getParent()).getUserObject();
            Set<String> migrations = manager.migrateUp((String) parentUserObject, count);
        }
    }

    @Override
    public boolean isEnabled() {
        DefaultMutableTreeNode treeNode = getSelectedNode();
        if (treeNode == null) {
            return false;
        }

        Object userObject = treeNode.getUserObject();
        if (userObject instanceof Migration) {
            return ((Migration) userObject).status != MigrationStatus.Success;
        }

        if (userObject instanceof String) {
            Enumeration migrationEnumeration = treeNode.children();
            while (migrationEnumeration.hasMoreElements()) {
                Object tmp = ((DefaultMutableTreeNode) migrationEnumeration.nextElement()).getUserObject();
                if (tmp instanceof Migration) {
                    if (((Migration) tmp).status != MigrationStatus.Success) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private DefaultMutableTreeNode getSelectedNode() {
        MigrationPanel panel = (MigrationPanel) getContextComponent();
        JTree tree = panel.getTree();

        if (tree.getSelectionModel().getSelectionCount() > 0) {
            TreePath leadSelectionPath = tree.getLeadSelectionPath();
            if (leadSelectionPath == null) {
                return null;
            }

            return (DefaultMutableTreeNode) leadSelectionPath.getLastPathComponent();
        }

        return null;
    }
}
