{   if ($1 == "<!--PHP-1-->") {
    	 printf("  <td bgcolor=\"#eeeeff\" valign=\"top\" width=\"25%\">\n");
    	 printf("  <center>\n");
    	 printf("  <br><b>Latest News </b> <br>\n");
    	 printf("  </center>\n");
 printf("  <?php include('http://sourceforge.net/export/projnews.php?group_id=42080&limit=6&flat=1&show_summaries=1'); ?>\n");
	 printf("  </td>\n");
    } else {
        print $0;
    }
}
