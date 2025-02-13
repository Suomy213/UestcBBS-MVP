package com.scatl.uestcbbs.widget.span;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.module.board.view.SingleBoardActivity;
import com.scatl.uestcbbs.module.collection.view.CollectionActivity;
import com.scatl.uestcbbs.module.credit.view.CreditHistoryActivity;
import com.scatl.uestcbbs.module.magic.view.MagicShopActivity;
import com.scatl.uestcbbs.module.post.view.NewPostDetailActivity;
import com.scatl.uestcbbs.module.post.view.ViewVoterFragment;
import com.scatl.uestcbbs.module.task.view.TaskActivity;
import com.scatl.uestcbbs.module.user.view.UserDetailActivity;
import com.scatl.uestcbbs.module.webview.view.WebViewActivity;
import com.scatl.uestcbbs.util.ColorUtil;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.ForumUtil;
import com.scatl.uestcbbs.util.SharePrefUtil;
import com.scatl.uestcbbs.util.TimeUtil;

public class MyClickableSpan extends ClickableSpan {
    private String url;
    private Context context;
    private boolean underLine = true;
    private int color;

    public MyClickableSpan(Context context, String url) {
        this(context, url, true);
    }

    public MyClickableSpan(Context context, String url, int color) {
        this(context, url, true);
        this.color = color;
    }

    public MyClickableSpan(Context context, String url, boolean underLine) {
        this.context = context;
        this.underLine = underLine;
        this.url = url.replaceAll(" ", "").replaceAll("\n", "");
        this.color = ColorUtil.getAttrColor(context, R.attr.colorPrimary);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(underLine);
        ds.setColor(color);
    }

    @Override
    public void onClick(@NonNull View widget) {

        ForumUtil.ForumLink forumLink = ForumUtil.getFromLinkInfo(url);

        if (Constant.TASK_LINK.equals(url)) {
            context.startActivity(new Intent(context, TaskActivity.class));
        } else if (Constant.MAGIC_SHOP_LINK.equals(url)){
            context.startActivity(new Intent(context, MagicShopActivity.class));
        } else if (Constant.CREDIT_HISTORY_LINK.equals(url)) {
            context.startActivity(new Intent(context, CreditHistoryActivity.class));
        }  else if (Constant.VIEW_VOTER_LINK.equals(url)) {
            if (context instanceof FragmentActivity && widget.getTag() instanceof Bundle) {
                ViewVoterFragment
                        .getInstance((Bundle) widget.getTag())
                        .show(((FragmentActivity)context).getSupportFragmentManager(), TimeUtil.getStringMs());
            }
        } else if (forumLink.linkType == ForumUtil.LinkType.OTHER) {
            if (SharePrefUtil.isOpenLinkByInternalBrowser(context)) {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(Constant.IntentKey.URL, url);
                context.startActivity(intent);
            } else {
                CommonUtil.openBrowser(context, url);
            }
        } else if (forumLink.linkType == ForumUtil.LinkType.POST) {
            Intent intent = new Intent(context, NewPostDetailActivity.class);
            intent.putExtra(Constant.IntentKey.TOPIC_ID, forumLink.id);
            context.startActivity(intent);
        } else if (forumLink.linkType == ForumUtil.LinkType.FORUM) {
            Intent intent = new Intent(context, SingleBoardActivity.class);
            intent.putExtra(Constant.IntentKey.BOARD_ID, forumLink.linkType);
            context.startActivity(intent);
        } else if (forumLink.linkType == ForumUtil.LinkType.USER) {
            Intent intent = new Intent(context, UserDetailActivity.class);
            intent.putExtra(Constant.IntentKey.USER_ID, forumLink.id);
            context.startActivity(intent);
        } else if (forumLink.linkType == ForumUtil.LinkType.COLLECTION) {
            Intent intent = new Intent(context, CollectionActivity.class);
            intent.putExtra(Constant.IntentKey.COLLECTION_ID, forumLink.id);
            context.startActivity(intent);
        } else if (forumLink.linkType == ForumUtil.LinkType.PID) {
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.putExtra(Constant.IntentKey.URL, url);
            context.startActivity(intent);
        }
    }
}
