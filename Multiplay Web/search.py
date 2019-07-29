import spotipy
import json
from spotipy.oauth2 import SpotifyClientCredentials
import os

class SpotifySearch:
    id = os.environ.get("WEB_KEY")
    secret = os.environ.get("WEB_SECRET")
    if (id == None): # aka if running on local
        file = open("config.txt", "r")
        api = file.read().split("\n")
        id = api[0]
        secret = api[1]
    client_credentials_manager = SpotifyClientCredentials(
                            client_id=id,
                            client_secret = secret)
    sp = spotipy.Spotify(client_credentials_manager=client_credentials_manager)

    def search(self, query, category="track"):
        spotify_results = self.sp.search(q=query+"*", type=category, limit=10)
        results = spotify_results[category + "s"]["items"]
        desired = ["name", "artists", "uri", "album"]

        output = [{k:v for k,v in results[i].items() if k in desired} for i in range(len(results))]

        for i in range(len(output)):
            output[i]["album"] = output[i]["album"]["images"][-1]["url"]
            artists = [artist["name"] for artist in output[i]["artists"]]
            output[i]["artists"] = artists

        return output
