package titans17576.ftcrc7573

/*import kotlinx.coroutines.CoroutineScope
import org.openftc.revextensions2.ExpansionHubEx
import org.openftc.revextensions2.RevBulkData

class RevBulkProvider(rev: ExpansionHubEx, op_mode: AsyncOpMode) {
    val rev = rev;
    val op_mode = op_mode
    private lateinit var bulk_data: RevBulkData
    val source: FeedSource<RevBulkData> = FeedSource(rev.bulkInputData)

    private suspend fun do_yes(): Unit {
        source.update(rev.bulkInputData)
        op_mode.launch(double_do_yes)
    }
    private val double_do_yes: suspend CoroutineScope.() -> Unit = { do_yes() }
    init {
        op_mode.launch(double_do_yes)
    }

    fun fork() = FeedListener(this.source)
}*/

class RevBulkProvider(rev: Any, op_mode: AsyncOpMode) {

}