package codedriver.module.tenant.api.test;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;


import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.core.ApiComponentBase;


@Service
public class TestApi extends ApiComponentBase {

	@Override
	public String getToken() {
		return "test";
	}

	@Override
	public String getName() {
		return "测试输出log接口";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Description(desc = "测试输出log接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String result = "        var blog_address = \"https://blog.csdn.net/ttt111zzz\";\r\n" + 
				"        var static_host = \"https://csdnimg.cn/release/phoenix/\";\r\n" + 
				"        var currentUserName = \"baopu1400\";\r\n" + 
				"        var isShowAds = true;\r\n" + 
				"        var isOwner = false;\r\n" + 
				"        var loginUrl = \"http://passport.csdn.net/account/login?from=https://blog.csdn.net/ttt111zzz/article/details/73289267\"\r\n" + 
				"        var blogUrl = \"https://blog.csdn.net/\";\r\n" + 
				"\r\n" + 
				"        var curSkin = \"skin-yellow\";\r\n" + 
				"        // 收藏所需数据\r\n" + 
				"        var articleTitle = \"GitLab创建备份与恢复\";\r\n" + 
				"        var articleDesc = \"GitLab创建备份与恢复\";\r\n" + 
				"\r\n" + 
				"        var articleTitles = \"GitLab创建备份与恢复_单调的低调的博客-CSDN博客\";\r\n" + 
				"        \r\n" + 
				"        var nickName = \"单调的低调\";\r\n" + 
				"        var isCorporate = false;\r\n" + 
				"        var subDomainBlogUrl = \"https://blog.csdn.net/\"\r\n" + 
				"        var digg_base_url = \"https://blog.csdn.net/ttt111zzz/phoenix/comment\";\r\n" + 
				"        var articleDetailUrl = \"https://blog.csdn.net/ttt111zzz/article/details/73289267\";\r\n" + 
				"        var isShowThird = \"0\"\r\n" + 
				"    </script>\r\n" + 
				"    <script src=\"https://csdnimg.cn/public/common/libs/jquery/jquery-1.9.1.min.js\" type=\"text/javascript\"></script>\r\n" + 
				"    <!--js引用-->\r\n" + 
				"            <script src=\"//g.csdnimg.cn/??fixed-sidebar/1.1.6/fixed-sidebar.js,report/1.4.1/report.js\" type=\"text/javascript\"></script>\r\n" + 
				"    <link rel=\"stylesheet\" href=\"https://csdnimg.cn/public/sandalstrap/1.4/css/sandalstrap.min.css\">\r\n" + 
				"    <style>\r\n" + 
				"        .MathJax, .MathJax_Message, .MathJax_Preview{\r\n" + 
				"            display: none\r\n" + 
				"        }\r\n" + 
				"    </style>\r\n" + 
				"</head>\r\n" + 
				"<body class=\"nodata \" > \r\n" + 
				"    <link rel=\"stylesheet\" href=\"https://csdnimg.cn/public/common/toolbar/content_toolbar_css/content_toolbar.css\">\r\n" + 
				"    <script id=\"toolbar-tpl-scriptId\" src=\"https://csdnimg.cn/public/common/toolbar/js/content_toolbar.js\" type=\"text/javascript\" domain=\"https://blog.csdn.net/\"></script>\r\n" + 
				"    <script>\r\n" + 
				"    (function(){\r\n" + 
				"        var bp = document.createElement('script');\r\n" + 
				"        var curProtocol = window.location.protocol.split(':')[0];\r\n" + 
				"        if (curProtocol === 'https') {\r\n" + 
				"            bp.src = 'https://zz.bdstatic.com/linksubmit/push.js';\r\n" + 
				"        }\r\n" + 
				"        else {\r\n" + 
				"            bp.src = 'http://push.zhanzhang.baidu.com/push.js';\r\n" + 
				"        }\r\n" + 
				"        var s = document.getElementsByTagName(\"script\")[0];\r\n" + 
				"        s.parentNode.insertBefore(bp, s);\r\n" + 
				"    })();\r\n" + 
				"</script>\r\n" + 
				"<link rel=\"stylesheet\" href=\"https://csdnimg.cn/release/phoenix/template/css/blog_code-c3a0c33d5c.css\">\r\n" + 
				"<link rel=\"stylesheet\" href=\"https://csdnimg.cn/release/phoenix/vendor/pagination/paging-e040f0c7c8.css\">\r\n" + 
				"\r\n" + 
				"<script type=\"text/javascript\">\r\n" + 
				"	var NEWS_FEED = function(){}\r\n" + 
				"</script>\r\n" + 
				"\r\n" + 
				"<link rel=\"stylesheet\" href=\"https://csdnimg.cn/release/phoenix/template/css/chart-3456820cac.css\" />\r\n" + 
				"<div class=\"main_father clearfix d-flex justify-content-center\" style=\"height:100%;\"> \r\n" + 
				"    <div class=\"container clearfix\" id=\"mainBox\">\r\n" + 
				"        <div  class='space_container'></div>\r\n" + 
				"        <main>\r\n" + 
				"            <div class=\"blog-content-box\">\r\n" + 
				"    <div class=\"article-header-box\">\r\n" + 
				"        <div class=\"article-header\">\r\n" + 
				"            <div class=\"article-title-box\">\r\n" + 
				"                <h1 class=\"title-article\">GitLab创建备份与恢复</h1>\r\n" + 
				"            </div>\r\n" + 
				"            <div class=\"article-info-box\">\r\n" + 
				"                <div class=\"article-bar-top\">\r\n" + 
				"                    <!--文章类型-->\r\n" + 
				"                    <span class=\"article-type type-1 float-left\">原创</span>                                                                                                                                            <a class=\"follow-nickName\" href=\"https://me.csdn.net/ttt111zzz\" target=\"_blank\" rel=\"noopener\">单调的低调</a>\r\n" + 
				"                                        <span class=\"time\">发布于2017-06-15 15:07:15                    </span>\r\n" + 
				"                    <span class=\"read-count\">阅读数 224</span>\r\n" + 
				"                    <a id='blog_detail_zk_collection' data-report-click='{\"mod\":\"popu_823\"}'>\r\n" + 
				"                        <svg class=\"icon\">\r\n" + 
				"                            <use xlink:href=\"#icon-csdnc-Collection-G\" ></use>\r\n" + 
				"                        </svg>\r\n" + 
				"                        收藏\r\n" + 
				"                    </a>\r\n" + 
				"                </div>\r\n" + 
				"                <div class=\"up-time\">更新于2017-06-15 15:08:00</div>\r\n" + 
				"                <div class=\"slide-content-box\">\r\n" + 
				"                                        <div class=\"tags-box artic-tag-box\">\r\n" + 
				"                           <span class=\"label\">分类专栏：</span>\r\n" + 
				"                                                                                             <a class=\"tag-link\" target=\"_blank\" rel=\"noopener\"\r\n" + 
				"                                      href=\"https://blog.csdn.net/ttt111zzz/category_6974242.html\">\r\n" + 
				"                                       GitLab                                   </a>\r\n" + 
				"                                                                                  </div>\r\n" + 
				"                                                                                                                                                       <div class=\"article-copyright\">\r\n" + 
				"                        <span class=\"creativecommons\">\r\n" + 
				"                            <a rel=\"license\" href=\"http://creativecommons.org/licenses/by-sa/4.0/\"></a>\r\n" + 
				"                            <span>\r\n" + 
				"                                版权声明：本文为博主原创文章，遵循<a href=\"http://creativecommons.org/licenses/by-sa/4.0/\" target=\"_blank\" rel=\"noopener\"> CC 4.0 BY-SA </a>版权协议，转载请附上原文出处链接和本声明。                            </span>\r\n" + 
				"                            <div class=\"article-source-link2222\">\r\n" + 
				"                                本文链接：<a href=\"https://blog.csdn.net/ttt111zzz/article/details/73289267\">https://blog.csdn.net/ttt111zzz/article/details/73289267</a>\r\n" + 
				"                            </div>\r\n" + 
				"                        </span> \r\n" + 
				"                        </div>\r\n" + 
				"                                                                                </div>\r\n" + 
				"                <div class=\"operating\">\r\n" + 
				"                                        <a class=\"href-article-edit slide-toggle\">展开</a>\r\n" + 
				"                </div>\r\n" + 
				"            </div>\r\n" + 
				"        </div>\r\n" + 
				"    </div>\r\n" + 
				"    <article class=\"baidu_pl\">\r\n" + 
				"        <!--python安装手册开始-->\r\n" + 
				"                <!--python安装手册结束-->\r\n" + 
				"                <!--####专栏广告位图文切换开始-->\r\n" + 
				"                                    <div class=\"blog-column-pay\">\r\n" + 
				"                    <a data-report-view='{\"mod\":\"popu_834\",\"dest\":\"https://blog.csdn.net/csdngkk/article/details/103682497\",\"strategy\":\"pc文章详情页博客之星广告位\",\"extend1:{\"user_type\":\"1\"}\"}' data-report-click='{\"mod\":\"popu_834\",\"dest\":\"https://blog.csdn.net/csdngkk/article/details/103682497\",\"strategy\":\"pc文章详情页博客之星广告位\",\"extend1:{\"user_type\":\"1\"}\"}' target=\"_blank\" data-report-query=\"utm_source=bkzx_BWzd\" href=\"https://blog.csdn.net/csdngkk/article/details/103682497\">\r\n" + 
				"                        <img style=\"width:100%\" src=\"https://img-blog.csdnimg.cn/20191225111311593.png\"/>\r\n" + 
				"                    </a>\r\n" + 
				"                </div>\r\n" + 
				"                            <!--####专栏广告位图文切换结束-->\r\n" + 
				"         <div id=\"article_content\" class=\"article_content clearfix\">\r\n" + 
				"            <link rel=\"stylesheet\" href=\"https://csdnimg.cn/release/phoenix/template/css/ck_htmledit_views-833878f763.css\" />\r\n" + 
				"                                        <link rel=\"stylesheet\" href=\"https://csdnimg.cn/release/phoenix/template/css/ck_htmledit_views-833878f763.css\" />\r\n" + 
				"                <div class=\"htmledit_views\" id=\"content_views\">\r\n" + 
				"                                            \r\n" + 
				"<div style=\"margin-left:28px;text-align:center;line-height:1.75;font-size:14px;\">\r\n" + 
				"<br /></div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><span style=\"font-size:16px;color:rgb(51,51,51);\"><strong>1. 完整的 Gitlab 备份:</strong></span></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><span style=\"font-size:16px;color:rgb(223,64,42);\">gitlab-rake gitlab:</span><span style=\"font-size:16px;color:rgb(223,64,42);\"><strong>backup</strong></span><span style=\"font-size:16px;color:rgb(223,64,42);\">:</span><span style=\"font-size:16px;color:rgb(223,64,42);\"><strong>create</strong></span><span style=\"font-size:13px;color:rgb(51,51,51);\"></span></div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><span style=\"color:rgb(51,51,51);\">使用以上命令会在</span><span style=\"font-size:13px;color:rgb(199,37,78);\">/var/opt/gitlab/backups</span><span style=\"color:rgb(51,51,51);\">目录下创建一个名称类似为</span><span style=\"font-size:13px;color:rgb(154,188,82);\">1393513186_gitlab_backup.tar</span><span style=\"color:rgb(51,51,51);\">的压缩包,\r\n" + 
				" 这个压缩包就是Gitlab整个的完整部分, 其中开头的</span><span style=\"font-size:13px;color:rgb(199,37,78);\">1393513186</span><span style=\"color:rgb(51,51,51);\">是备份创建的日期.</span></div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><br /></div>\r\n" + 
				"<div style=\"line-height:1.2;font-size:14px;\"><span style=\"font-size:16px;\"><strong>2.</strong></span><span style=\"font-size:16px;color:rgb(51,51,51);\"><strong>\r\n" + 
				"</strong></span><span style=\"font-size:16px;\"><strong>Gitlab 修改备份文件默认目录</strong></span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">通过修改 <span style=\"color:rgb(223,64,42);\">\r\n" + 
				"/etc/gitlab/gitlab.rb</span> 来修改默认存放备份文件的目录:</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">gitlab_rails['backup_path'] = '/home/user/backups '</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">/home/user/backups</span> 修改为你想存放备份的目录,</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">修改完成之后使用<span style=\"color:rgb(223,64,42);\"> gitlab-ctl reconfigure\r\n" + 
				"</span>命令重载配置文件即可.</div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><br /></div>\r\n" + 
				"<div style=\"line-height:1.2;font-size:14px;\"><span style=\"font-size:16px;color:rgb(51,51,51);\"><strong>3. Gitlab 自动备份(默认备份方法)</strong></span></div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><span style=\"color:rgb(51,51,51);\">也可以通过</span><span style=\"font-size:13px;color:rgb(199,37,78);\">crontab</span><span style=\"color:rgb(51,51,51);\">使用备份命令实现自动备份:</span></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">sudo su -crontab -e</span><span style=\"font-size:13px;color:rgb(51,51,51);\"></span></div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><span style=\"color:rgb(51,51,51);\">加入以下, 实现每天凌晨2点进行一次自动备份:</span></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">0 2 * * * /opt/gitlab/bin/gitlab-rake gitlab:backup:create</span></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><br /></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><span style=\"font-size:16px;\"><strong>3.</strong></span><span style=\"font-size:16px;color:rgb(51,51,51);\"><strong>\r\n" + 
				"</strong></span><span style=\"font-size:16px;\"><strong>Gitlab 自动备份（脚本备份方法）</strong></span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">从<a href=\"https://github.com/sund/auto-gitlab-backup\" rel=\"nofollow\"><span style=\"font-size:10px;color:rgb(0,56,132);\">https://github.com/sund/auto-gitlab-backup</span></a>下载<span style=\"color:rgb(154,188,82);\">auto-gitlab-backup-master.zip</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">把 <span style=\"color:rgb(154,188,82);\">\r\n" + 
				"auto-gitlab-backup-master.zip</span> 文件放到备份目录，并解压，把<span style=\"color:rgb(154,188,82);\">auto-gitlab-backup.conf.sample</span>复制成<span style=\"color:rgb(154,188,82);\">auto-gitlab-backup.conf</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">通过<span style=\"color:rgb(223,64,42);\">crontab</span>使用备份命令实现自动备份:</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">sudo su</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">crontab -e</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">加入以下, 实现每天凌晨3点进行一次自动备份:</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">0 3 * * * /home/user/backups/auto-gitlab-backup-master/auto-gitlab-backup.sh</span></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><span style=\"font-size:13px;color:rgb(51,51,51);\"></span></div>\r\n" + 
				"<div style=\"line-height:1.2;font-size:14px;\"><span style=\"font-size:16px;\"><strong>4. Gitlab 恢复</strong></span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\">同样, Gitlab的从备份恢复也非常简单:</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"># 停止相关数据连接服务</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">gitlab-ctl stop unicorn</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">gitlab-ctl stop sidekiq</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><br /></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"># 从<span style=\"color:rgb(154,188,82);\">1491469575_2017_04_06</span>编号备份中恢复</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">gitlab-rake gitlab:backup:restore BACKUP=1491469575_2017_04_06</span></div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"> </div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"># 启动Gitlab</div>\r\n" + 
				"<div style=\"line-height:1.75;font-size:14px;\"><span style=\"color:rgb(223,64,42);\">sudo gitlab-ctl start</span></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><br /></div>\r\n" + 
				"<div style=\"line-height:1.3;font-size:14px;\"><span style=\"font-size:13px;color:rgb(51,51,51);\"></span></div>\r\n" + 
				"<div style=\"line-height:1.2;font-size:14px;\"><span style=\"font-size:17px;color:rgb(51,51,51);\"><strong>Gitlab迁移</strong></span></div>\r\n" + 
				"<div style=\"line-height:1.6;font-size:14px;\"><span style=\"color:rgb(51,51,51);\">迁移如同备份与恢复的步骤一样, 只需要将老服务器</span><span style=\"font-size:13px;color:rgb(199,37,78);\">/var/opt/gitlab/backups</span><span style=\"color:rgb(51,51,51);\">目录下的备份文件拷贝到新服务器上的</span><span style=\"font-size:13px;color:rgb(199,37,78);\">/var/opt/gitlab/backups</span><span style=\"color:rgb(51,51,51);\">即可(如果你没修改过默认备份目录的话).\r\n" + 
				" 但是需要注意的是新服务器上的Gitlab的版本必须与创建备份时的Gitlab版本号相同. 比如新服务器安装的是最新的7.60版本的Gitlab, 那么迁移之前, 最好将老服务器的Gitlab 升级为7.60在进行备份.</span></div>\r\n" + 
				"                                    </div>\r\n" + 
				"                                    <div class=\"more-toolbox\">";
		return result;
	}

}
