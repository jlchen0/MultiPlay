import spotipy
import json
from spotipy.oauth2 import SpotifyClientCredentials
import os

class SpotifySearch:
    config = open("config.txt", "r").read()
    id = config.split("\n")[0]
    secret = config.split("\n")[1]
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
