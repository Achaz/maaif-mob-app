// Generated code from Butter Knife. Do not modify!
package ug.go.agriculture.MAAIF_Extension.daes.dairy;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;
import ug.go.agriculture.MAAIF_Extension.R;

public class PhotoUpload_ViewBinding implements Unbinder {
  private PhotoUpload target;

  private View view7f0a04a3;

  private View view7f0a04a0;

  @UiThread
  public PhotoUpload_ViewBinding(PhotoUpload target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public PhotoUpload_ViewBinding(final PhotoUpload target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.user_profile_photo, "field 'userProfilePhoto' and method 'onViewClicked'");
    target.userProfilePhoto = Utils.castView(view, R.id.user_profile_photo, "field 'userProfilePhoto'", ImageButton.class);
    view7f0a04a3 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
    target.txtProgress = Utils.findRequiredViewAsType(source, R.id.textViewProgress, "field 'txtProgress'", TextView.class);
    view = Utils.findRequiredView(source, R.id.upload_file_progress, "field 'uploadFileProgress' and method 'onViewClicked'");
    target.uploadFileProgress = Utils.castView(view, R.id.upload_file_progress, "field 'uploadFileProgress'", Button.class);
    view7f0a04a0 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onViewClicked(p0);
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    PhotoUpload target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.userProfilePhoto = null;
    target.txtProgress = null;
    target.uploadFileProgress = null;

    view7f0a04a3.setOnClickListener(null);
    view7f0a04a3 = null;
    view7f0a04a0.setOnClickListener(null);
    view7f0a04a0 = null;
  }
}
