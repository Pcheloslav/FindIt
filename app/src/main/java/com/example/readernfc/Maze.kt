package com.example.readernfc

import android.graphics.*
import org.json.JSONArray
import org.json.JSONStringer
import org.json.JSONTokener

// 2D matrix of cells with serialization, maze search and drawing logic
class Maze(
    json: String, // String with matrix data in JSON format
    private val bitmapW: Float, // Width of output bitmap picture
    private val bitmapH: Float, // Height of output bitmap picture
    private val icons: Map<String, Bitmap> // Dictionary of icon names and icons
) {
    val cells: Array<Array<Cell>> // 2D matrix
    val h: Int // Logical height, i.e. number of rows
    val w: Int // Logical width, i.e. number of columns
    private val cellW: Float // Width of cell in picture
    private val cellH: Float // Height of cell in picture

    // Deserialize maze from json
    init {
        // Parse outer json array
        val jsonMat = JSONTokener(json).nextValue() as JSONArray
        cells = Array(jsonMat.length()) { i ->
            // Parse inner json array
            val jsonRow = jsonMat.getJSONArray(i)
            Array(jsonRow.length()) { j ->
                // Parse json object
                val jsonObj = jsonRow.getJSONObject(j)
                val isRoad = jsonObj.getBoolean("isRoad")
                val isEnd = jsonObj.getBoolean("isEnd")
                val iconName = jsonObj.getString("iconName")
                val cols = jsonObj.getInt("cols")
                val rows = jsonObj.getInt("rows")

                // New object of Cell class
                Cell(isRoad, isEnd, iconName, cols, rows)
            }
        }
        h = cells.size
        w = cells[0].size
        cellH = bitmapH / h
        cellW = bitmapW / w
    }

    // Serialize maze to json
    fun getJson(): String {
        val jsonStringer = JSONStringer()

        // Write matrix
        jsonStringer.array()
        cells.forEach { row ->

            // Write row
            jsonStringer.array()
            row.forEach { cell ->
                jsonStringer.`object`()

                // Write object
                jsonStringer.key("isRoad")
                jsonStringer.value(cell.isRoad)
                jsonStringer.key("isEnd")
                jsonStringer.value(cell.isEnd)
                jsonStringer.key("iconName")
                jsonStringer.value(cell.iconName)
                jsonStringer.key("cols")
                jsonStringer.value(cell.cols)
                jsonStringer.key("rows")
                jsonStringer.value(cell.rows)

                jsonStringer.endObject()
            }
            jsonStringer.endArray()
        }
        jsonStringer.endArray()
        return jsonStringer.toString()
    }

    // Find shortest path via bfs
    fun findPath(start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {

        // Init data
        val path = arrayListOf<Pair<Int, Int>>()
        val isVisited = Array(h) { BooleanArray(w) { false } }
        val parent = Array(h) { Array(w) { Pair(-1, -1) } }

        // Find path
        var isFound = false
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(start)
        while (!queue.isEmpty()) {

            // Visit tile
            val (x, y) = queue.removeFirst()
            isFound = Pair(x, y) == end
            if (isFound) {
                queue.clear()
                break
            }
            // Enqueue neighbours
            val neighbours = arrayOf(
                Pair(x + 1, y),
                Pair(x, y + 1),
                Pair(x - 1, y),
                Pair(x, y - 1)
            )
            neighbours.forEach {
                // Not out of range & is traversable & is not visited
                val canVisit = it.first in 0 until w &&
                        it.second in 0 until h &&
                        (cells[it.second][it.first].isRoad && !isVisited[it.second][it.first]
                                || it == end)
                if (canVisit) {
                    // Enqueueing
                    isVisited[it.second][it.first] = true
                    parent[it.second][it.first] = Pair(x, y)
                    queue.add(it)
                }
            }
        }

        // Restore path
        if (isFound) {
            var current = end
            while (current != start) {
                path.add(current)
                current = parent[current.second][current.first]
            }
            path.add(start)
            path.reverse()
        }

        return path
    }
    fun getMarks():ArrayList<RectF> {
        val marks = ArrayList<RectF>()
        cells.forEachIndexed { i, row ->
            row.forEachIndexed { j, cell ->
                if (cell.isEnd) {
                    marks.add(RectF(cellW *j, cellH*i, cellW*(j+1), cellH*(i+1)))
                }
            }
        }
        return marks
    }

    // Draw maze on new bitmap
    fun drawMaze(): Bitmap {

        // Drawing data
        val bitmap = Bitmap.createBitmap(bitmapW.toInt(), bitmapH.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRGB(255, 255, 255)

        // Iterate over rows
        var y = 0F
        cells.forEach { row ->

            // Iterate over cells
            var x = 0F
            row.forEach { cell ->

                // Draw icon
                if (cell.iconName in icons.keys) {
                    val bounds = RectF(x, y, x + cellW * cell.cols, y + cellH * cell.rows)
                    canvas.drawBitmap(icons[cell.iconName]!!, null, bounds, null)
                }
                x += cellW
            }
            y += cellH
        }
        return bitmap
    }

    // Draw maze and path on new bitmap
    fun drawPath(path: List<Pair<Int, Int>>): Bitmap {

        // Drawing data
        val bitmap = drawMaze()
        val canvas = Canvas(bitmap)
        val paint = Paint()
        with(paint) {
            color = Color.GREEN
            strokeWidth = 7F
        }

        // Draw lines between adjacent cells' centers
        for (i in 0 until path.size - 1) {
            val x1 = cellW * (path[i].first + .5F)
            val y1 = cellH * (path[i].second + .5F)
            val x2 = cellW * (path[i + 1].first + .5F)
            val y2 = cellH * (path[i + 1].second + .5F)
            canvas.drawLine(x1, y1, x2, y2, paint)
        }
        return bitmap
    }
}

