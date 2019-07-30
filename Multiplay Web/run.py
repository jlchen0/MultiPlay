from flask import Flask, render_template, request, jsonify, url_for, redirect
from flask_caching import Cache
import search
import os
import redis

app = Flask(__name__)

# cache = Cache(app, config={"CACHE_TYPE": "simple"})
cache = redis.from_url(os.environ["REDISCLOUD_URL"])

@app.route("/", methods=["POST", "GET"])
def index():
    # check if Lobby Name form is submitted via POST
    if request.method == "POST":
        lobby_name = request.form["Lobby Name"]
        return redirect(url_for("show_lobby_name", lobby_name=lobby_name))
    # otherwise return regular index.html
    return render_template("index.html")

# opens a new lobby page with given lobby name
@app.route("/lobby/<lobby_name>")
def show_lobby_name(lobby_name):
    return render_template("lobby.html", name=lobby_name)

@app.route("/test")
def show_test():
    return  render_template("test.html")


@app.route("/search", methods=["POST", "GET"])
def search_query():
    if (request.method == "POST"):
        query = request.form["query"]
        print("requesting", query)
        search_client = search.SpotifySearch()
        search_results = search_client.search(query)
    return jsonify(search_results)


@app.route("/songs/<lobby_name>", methods=["POST", "GET"])
def show_songs(lobby_name):
    if request.method == "POST":
        song_uri = request.form["song_uri"]
        print(cache.lrange(lobby_name, 0, -1)) # print out current results
        print("Submitting song", song_uri)
        cache.rpush(lobby_name, song_uri)
    current_queue = [x.decode("utf-8") for x in cache.lrange(lobby_name, 0, -1)]

    return jsonify(current_queue)

    """
    ###### W/  GLOBAL VARIABLE SONG_QUEUES
    global song_queues
    if request.method == "POST":

        song_uri = request.form["song_uri"]
        # print(song_uri)
        if song_queues.get(lobby_name) == None:
            song_queues[lobby_name] = []
        song_queues[lobby_name].append(song_uri)
        print("Submitting song", song_uri)
    print(song_queues.keys())
    return jsonify(song_queues[lobby_name])

_________________________________________________________________
    if request.method == "POST":
        # check if POST method is from Submit Song button
        if request.form["action"] == "Search":
            result = request.form["Song Name"]
            if song_queues.get(lobby_name) == None:
                song_queues[lobby_name] = []
            song_queues[lobby_name].append(result)
            print("Submitting song", result)


        elif request.form["action"] == "Clear Queue":
            song_queues[lobby_name] = []

        Permission to clear queue is not granted to all users. Only given to master phone, through android app.
        """
