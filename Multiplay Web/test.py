import search

search_client = search.SpotifySearch()
search_results = search_client.search("dayton")

for song in search_results:
    print(song)
