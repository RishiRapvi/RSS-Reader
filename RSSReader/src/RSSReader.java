import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Rishi Singhvi
 *
 *         Sources: - https://www.w3schools.com/xml/xml_rss.asp -
 *         https://www.w3schools.com/xml/rss_tag_title_link_description_item.asp
 *         - https://www.rssboard.org/rss-specification
 */
public final class RSSReader {

    /**
     * Private constructor to prevent instantiation.
     */
    private RSSReader() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file.
     *
     * @param channel
     *            The channel element XMLTree.
     * @param out
     *            The output stream.
     * @requires [the root of channel is a <channel> tag] and out.isOpen
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.label().equals("channel") : "Violation of: root is <channel>";
        assert out.isOpen() : "Violation of: out.isOpen";

        out.println("<html><head><title>");
        int titleIndex = getChildIndex(channel, "title");
        out.println((titleIndex >= 0 && channel.child(titleIndex).numberOfChildren() > 0)
                ? channel.child(titleIndex).child(0).toString()
                : "No Title");
        out.println("</title></head><body>");

        int linkIndex = getChildIndex(channel, "link");
        out.print("<h1><a href=\""
                + (linkIndex >= 0 ? channel.child(linkIndex).child(0).toString() : "#")
                + "\">");
        out.print((titleIndex >= 0 && channel.child(titleIndex).numberOfChildren() > 0)
                ? channel.child(titleIndex).child(0).toString()
                : "No Title");
        out.println("</a></h1>");

        int descIndex = getChildIndex(channel, "description");
        out.println(
                "<p>" + (descIndex >= 0 && channel.child(descIndex).numberOfChildren() > 0
                        ? channel.child(descIndex).child(0).toString()
                        : "No Description") + "</p>");

        out.println(
                "<table border=\"1\"><tr><th>Date</th><th>Source</th><th>News</th></tr>");
    }

    /**
     * Outputs the "closing" tags in the generated HTML file.
     *
     * @param out
     *            The output stream.
     * @requires out.isOpen
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.isOpen";

        out.println("</table></body></html>");
    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and returns its index; returns -1 if not found.
     *
     * @param xml
     *            The {@code XMLTree} to search.
     * @param tag
     *            The tag to look for.
     * @return The index of the first child of type tag or -1 if not found.
     */
    private static int getChildIndex(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: xml root is a tag";

        for (int i = 0; i < xml.numberOfChildren(); i++) {
            if (xml.child(i).label().equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Processes one news item and outputs one table row.
     *
     * @param item
     *            The news item.
     * @param out
     *            The output stream.
     * @requires [the root of item is an <item> tag] and out.isOpen
     * @ensures out.content = #out.content * [an HTML table row with date,
     *          source, and title]
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.label().equals("item") : "Violation of: root is <item>";
        assert out.isOpen() : "Violation of: out.isOpen";

        out.println("<tr>");

        // Date
        int dateIndex = getChildIndex(item, "pubDate");
        out.print("<td>"
                + (dateIndex >= 0 ? item.child(dateIndex).child(0).toString() : "No Date")
                + "</td>");

        // Source
        int srcIndex = getChildIndex(item, "source");
        String src = (srcIndex >= 0) ? item.child(srcIndex).child(0).toString()
                : "No Source";
        String srcUrl = (srcIndex >= 0) ? item.child(srcIndex).attributeValue("url")
                : "#";
        out.print("<td>"
                + (srcIndex >= 0 ? "<a href=\"" + srcUrl + "\">" + src + "</a>" : src)
                + "</td>");

        // Title or Description
        int titleIndex = getChildIndex(item, "title");
        int descIndex = getChildIndex(item, "description");
        String news = (titleIndex >= 0) ? item.child(titleIndex).child(0).toString()
                : (descIndex >= 0 ? item.child(descIndex).child(0).toString()
                        : "No Title/Description");
        String linkUrl = getChildIndex(item, "link") >= 0
                ? item.child(getChildIndex(item, "link")).child(0).toString()
                : "#";
        out.println(
                "<td>" + (titleIndex >= 0 ? "<a href=\"" + linkUrl + "\">" + news + "</a>"
                        : news) + "</td>");

        out.println("</tr>");
    }

    /**
     * Main method.
     *
     * @param args
     *            Command line arguments.
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.println("Please enter the URL of an RSS 2.0 news feed: ");
        String url = in.nextLine();
        XMLTree rss = new XMLTree1(url);

        if (rss.label().equals("rss") && rss.hasAttribute("version")
                && rss.attributeValue("version").equals("2.0")) {
            XMLTree channel = rss.child(0);
            SimpleWriter fileOut = new SimpleWriter1L("output.html");

            outputHeader(channel, fileOut);

            // Process each <item> in the RSS feed
            for (int i = 0; i < channel.numberOfChildren(); i++) {
                if (channel.child(i).label().equals("item")) {
                    processItem(channel.child(i), fileOut);
                }
            }

            outputFooter(fileOut);
            fileOut.close();

        } else {
            out.println("Invalid RSS 2.0 feed.");
        }

        in.close();
        out.close();
    }
}
