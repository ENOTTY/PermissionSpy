package com.notatypewriter.permissionspy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class PermissionSpyActivity extends Activity {
	
	public static final String TAG = "PermissionSpyActivity";
	
	// Map of package names to permissions
	// XXX package names are not unique identifiers of apps...
	final Map<String, List<String>> pkgPerms =
			new HashMap<String, List<String>>();
	
    private static final String PKG_NAME = "PKG_NAME";
    private static final String PERM = "PERM";

    private ExpandableListAdapter mAdapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /* Grab data from PackageManager */
        
        PackageManager pm = getPackageManager();
        List<PackageInfo> currentPackages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS
        		| PackageManager.GET_UNINSTALLED_PACKAGES);
        /* We want uninstalled packages because if the the package previously
         * had sensitive permissions, the saved data might contain sensitive
         * info.
         */
        
        for (PackageInfo pkg : currentPackages) {
        	
        	// package has no permissions
        	if (pkg.requestedPermissions == null) {
        		pkgPerms.put(pkg.packageName, Collections.<String>emptyList());
        		/*continue;
        		 * Decided against this to ease understanding of the control
        		 * flow if I want to add something after this branch .
        		 */
        		
       		// package has permissions
        	} else {
        		pkgPerms.put(pkg.packageName, Arrays.asList(pkg.requestedPermissions));
        		/* I can't use pkgPerms.put(pkg.requestedPermissions)
        		 * because if pkg.requestedPermissions is null, then
        		 * Arrays.asList(null) throws a NullPointerException.
        		 */
        	}
        }
        
        /* Set up the UI adapter */
        
        List<Map<String, String>> groupData = 
        		new ArrayList<Map<String, String>>(pkgPerms.size());
        List<List<Map<String, String>>> childData =
        		new ArrayList<List<Map<String, String>>>(pkgPerms.size());
        
        for (String pkgName : pkgPerms.keySet()) {
            Map<String, String> curGroupMap = new HashMap<String, String>(1);
            groupData.add(curGroupMap);
        	curGroupMap.put(PKG_NAME, pkgName);
        	
        	List<Map<String, String>> children =
        			new ArrayList<Map<String, String>>(pkgPerms.get(pkgName).size());
        	
        	for (String perm : pkgPerms.get(pkgName)) {
        		Map<String, String> curChildMap = new HashMap<String, String>(1);
        		children.add(curChildMap);
        		curChildMap.put(PERM, perm);
        	}
        	if (children.isEmpty()) {
        		Map<String, String> tmp = new HashMap<String, String>(1);
        		tmp.put(PERM, "(none)");
        		children.add(tmp);
        	}
        	childData.add(children);
        }

        // Set up our adapter
        mAdapter = new SimpleExpandableListAdapter(
                this,
                groupData,
                R.layout.expandable_list_item_1_small,
                new String[] { PKG_NAME },
                new int[] { android.R.id.text1 },
                childData,
                R.layout.expandable_list_item_1_small,
                new String[] { PERM },
                new int[] { android.R.id.text1 }
                );
        ((ExpandableListView)findViewById(R.id.app_list)).setAdapter(mAdapter);
    }
}